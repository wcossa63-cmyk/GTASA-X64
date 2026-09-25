//
// Created on 01.02.2023.
//

#include "Dialog.h"
#include "main.h"

#include "../game/game.h"
#include "net/netgame.h"
#include "util/CJavaWrapper.h"

static jstring FixNewStringUTF(JNIEnv* env, const char* str) {
    if (!env || !str) return nullptr;

    size_t len = strlen(str);
    if (len == 0) return env->NewStringUTF("");

    std::string cleaned;

    for (size_t i = 0; i < len; i++) {
        unsigned char c = (unsigned char)str[i];

        if (c <= 0x7F) {
            if (c >= 0x20 || c == '\t' || c == '\n' || c == '\r') {
                cleaned += str[i];
            } else {
                cleaned += ' ';
            }
        }
        else if ((c & 0xE0) == 0xC0 && i + 1 < len) {
            if (((unsigned char)str[i + 1] & 0xC0) == 0x80) {
                cleaned += str[i];
                cleaned += str[i + 1];
                i++;
            } else {
                cleaned += '?';
            }
        }
        else if ((c & 0xF0) == 0xE0 && i + 2 < len) {
            if (((unsigned char)str[i + 1] & 0xC0) == 0x80 &&
                ((unsigned char)str[i + 2] & 0xC0) == 0x80) {
                cleaned += str[i];
                cleaned += str[i + 1];
                cleaned += str[i + 2];
                i += 2;
            } else {
                cleaned += '?';
            }
        }
        else if ((c & 0xF8) == 0xF0 && i + 3 < len) {
            if (((unsigned char)str[i + 1] & 0xC0) == 0x80 &&
                ((unsigned char)str[i + 2] & 0xC0) == 0x80 &&
                ((unsigned char)str[i + 3] & 0xC0) == 0x80) {
                cleaned += str[i];
                cleaned += str[i + 1];
                cleaned += str[i + 2];
                cleaned += str[i + 3];
                i += 3;
            } else {
                cleaned += '?';
            }
        }
        else {
            cleaned += '?';
        }
    }

    return env->NewStringUTF(cleaned.c_str());
}

void CDialog::show(int id, int style, char caption[], char info[], char button1[], char button2[])
{
    JNIEnv* env = g_pJavaWrapper->GetEnv();
    if (!env)
    {
        Log("No env");
        return;
    }

    jstring j_caption = FixNewStringUTF(env, caption);
    jstring j_info = FixNewStringUTF(env, info);
    jstring j_button1 = FixNewStringUTF(env, button1);
    jstring j_button2 = FixNewStringUTF(env, button2);

    jclass clazz = env->GetObjectClass(CDialog::thiz);
    jmethodID method = env->GetMethodID(clazz, "show", "(IILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");

    env->CallVoidMethod(CDialog::thiz, method, id, style, j_caption, j_info, j_button1, j_button2);

    env->DeleteLocalRef(j_caption);
    env->DeleteLocalRef(j_info);
    env->DeleteLocalRef(j_button1);
    env->DeleteLocalRef(j_button2);

    if(id != -1) CDialog::bIsShow = true;
}

void CDialog::rpcShowPlayerDialog(RPCParameters *rpcParams)
{
    unsigned char * Data = reinterpret_cast<unsigned char *>(rpcParams->input);
    int iBitLength = rpcParams->numberOfBitsOfData;

    uint16_t wDialogID = -1;
    uint8_t byteDialogStyle = 0;
    uint8_t len;
    char szBuff[4096+1];
    char title[64 * 3 + 1], info[4096+1], button1[20+1], button2[20+1];

    RakNet::BitStream bsData((unsigned char *)Data,(iBitLength/8)+1,false);

    bsData.Read(wDialogID);

    if(wDialogID == INVALID_PLAYER_ID) {
        CDialog::hide();
        return;
    }
    bsData.Read(byteDialogStyle);

    // title
    bsData.Read(len);
    bsData.Read(szBuff, len);
    szBuff[len] = '\0';
    cp1251_to_utf8(title, szBuff);

    // button1
    bsData.Read(len);
    bsData.Read(szBuff, len);
    szBuff[len] = '\0';
    cp1251_to_utf8(button1, szBuff);

    // button2
    bsData.Read(len);
    bsData.Read(szBuff, len);
    szBuff[len] = '\0';
    cp1251_to_utf8(button2, szBuff);

    // info
    stringCompressor->DecodeString(szBuff, 4096, &bsData);
    cp1251_to_utf8(info, szBuff);

    CDialog::show(wDialogID, byteDialogStyle, title, info, button1, button2);
}

void CDialog::hide() {
    JNIEnv* env = g_pJavaWrapper->GetEnv();

    CDialog::bIsShow = false;
    jclass clazz = env->GetObjectClass(CDialog::thiz);
    jmethodID method = env->GetMethodID(clazz, "hide", "()V");
    env->CallVoidMethod(CDialog::thiz, method);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_blrp_game_gui_dialogs_Dialog_init(JNIEnv *env, jobject thiz) {
    CDialog::thiz = env->NewGlobalRef(thiz);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_blrp_game_gui_dialogs_Dialog_sendResponse(JNIEnv *env, jobject thiz, jint button, jint id, jint item, jbyteArray str)
{
    jbyte* pMsg = env->GetByteArrayElements(str, nullptr);
    jsize length = env->GetArrayLength(str);

    std::string szStr((char*)pMsg, length);

    uint8_t respLen = strlen(szStr.c_str());

    RakNet::BitStream bsSend;
    bsSend.Write((uint16_t)id);
    bsSend.Write((uint8_t)button);
    bsSend.Write((uint16_t)item);

    bsSend.Write((uint8_t)respLen);
    bsSend.Write(const_cast<char *>(szStr.c_str()), respLen);

    pNetGame->m_pRakClient->RPC(&RPC_DialogResponse, &bsSend, HIGH_PRIORITY, RELIABLE, 0, false, UNASSIGNED_NETWORK_ID, NULL);

    env->ReleaseByteArrayElements(str, pMsg, JNI_ABORT);
    CDialog::bIsShow = false;
}
