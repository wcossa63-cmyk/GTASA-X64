//
// Created on 24.01.2023.
//

#include <jni.h>
#include "CLoader.h"
#include "util/patch.h"
#include "crashlytics.h"
#include "CSettings.h"
#include "net/netgame.h"
#include "java/Speedometr.h"
#include "java/ObjectEditor.h"
#include "java/Tab.h"
#include "java/GuiWrapper.h"
#include "GuiWrapper.h"
#include "SnapShotsWrapper.h"
#include "JavaGui.h"

void CLoader::loadBassLib()
{
    // LoadBassLibrary();
    // BASS_Init(-1, 44100, BASS_DEVICE_MONO | BASS_DEVICE_3D);
    //BASS_Set3DFactors(1, 0.15, 0);
    //BASS_Apply3D();
}

void CLoader::initCrashLytics()
{
    firebase::crashlytics::SetCustomKey("build data", __DATE__);
    firebase::crashlytics::SetCustomKey("build time", __TIME__);

    firebase::crashlytics::SetUserId(CSettings::m_Settings.szNickName);
    firebase::crashlytics::SetCustomKey("Nick", CSettings::m_Settings.szNickName);

    char str[100];

    sprintf(str, "0x%x", g_libGTASA);
    firebase::crashlytics::SetCustomKey("libGTASA.so", str);

    sprintf(str, "0x%x", g_libSAMP);
    firebase::crashlytics::SetCustomKey("libmultiplayer.so", str);

    //sprintf(str, "0x%x", libc);
    //firebase::crashlytics::SetCustomKey("libc.so", str);
}

void CLoader::loadSetting()
{
    CSettings::LoadSettings(nullptr);
}

jclass LinkJavaClass(jclass localObj) {
    auto env = CJavaWrapper::GetEnv();
    auto globalRef = (jclass)env->NewGlobalRef(localObj);
    env->DeleteLocalRef(localObj);
    return globalRef;
}


void CLoader::initJavaClasses(JavaVM* pjvm) {
    JNIEnv* env = nullptr;
    (void)pjvm->GetEnv((void**)&env, JNI_VERSION_1_6);

    CJavaGui::clazz = LinkJavaClass(env->FindClass("com/blrp/game/NewUiList"));

    SnapShotsWrapper::clazz = LinkJavaClass(env->FindClass("com/blrp/game/EntitySnaps"));

    CTab::clazz = LinkJavaClass(env->FindClass("com/blrp/game/gui/tab/Tab"));

    CSpeedometr::clazz = env->FindClass("com/blrp/game/gui/Speedometer");
    CSpeedometr::clazz = (jclass) env->NewGlobalRef( CSpeedometr::clazz );

    CObjectEditor::clazz = LinkJavaClass(env->FindClass("com/blrp/game/gui/AttachEdit"));
}
