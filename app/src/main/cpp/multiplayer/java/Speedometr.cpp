//
// Created on 11.02.2023.
//

#include "Speedometr.h"
#include "main.h"

#include "../game/game.h"
#include "net/netgame.h"
#include "util/CJavaWrapper.h"
#include "../game/Entity/Ped/Ped.h"

jclass  CSpeedometr::clazz = nullptr;
jobject CSpeedometr::thiz = nullptr;
bool    CSpeedometr::bIsShow = false;
bool    CSpeedometr::bIsNoNeedDraw = false;
float   CSpeedometr::fFuel = 0.0f;
int     CSpeedometr::iMilliage = 0;

void CSpeedometr::show()
{
    if( CSpeedometr::bIsShow ) return;
    Log("CSpeedometr::show()");
    JNIEnv* env = g_pJavaWrapper->GetEnv();

    jmethodID constructor = env->GetMethodID(CSpeedometr::clazz, "<init>", "()V");
    CSpeedometr::thiz = env->NewObject(CSpeedometr::clazz, constructor, g_pJavaWrapper->activity);
    CSpeedometr::thiz = env->NewGlobalRef(CSpeedometr::thiz);

    CSpeedometr::bIsShow = true;
}

void CSpeedometr::tempToggle(bool toggle)
{
    if( !CSpeedometr::bIsShow ) return;
    Log("CSpeedometr::tempToggle(%d)", toggle);

    bIsNoNeedDraw = !toggle;

    JNIEnv* env = g_pJavaWrapper->GetEnv();

    jmethodID method = env->GetMethodID(CSpeedometr::clazz, "tempToggle", "(Z)V");
    env->CallVoidMethod(CSpeedometr::thiz, method, toggle);
}

void CSpeedometr::hide()
{
    if( !CSpeedometr::bIsShow ) return;
    Log("CSpeedometr::hide()");
    JNIEnv* env = g_pJavaWrapper->GetEnv();

    jmethodID method = env->GetMethodID(CSpeedometr::clazz, "destroy", "()V");
    env->CallVoidMethod(CSpeedometr::thiz, method);

    env->DeleteGlobalRef(CSpeedometr::thiz);
    CSpeedometr::thiz = nullptr;

    CSpeedometr::bIsShow = false;
}

void CSpeedometr::UpdateSpeed()
{
    if( CSpeedometr::thiz == nullptr ) return;

    JNIEnv* env = g_pJavaWrapper->GetEnv();

    CPedSamp *pPed = CLocalPlayer::GetPlayerPed();
    if(!pPed) return;
    if(!pPed->m_pPed->IsInVehicle()) return;

    CVehicleSamp* pVehicle = pPed->GetCurrentVehicle();

    auto vecSpeed = pVehicle->m_pVehicle->GetMoveSpeed();
    auto speed = std::sqrt(vecSpeed.x * vecSpeed.x + vecSpeed.y * vecSpeed.y + vecSpeed.z * vecSpeed.z) * 179.1f;

    auto method = env->GetMethodID(clazz, "updateSpeed", "(I)V");
    env->CallVoidMethod(CSpeedometr::thiz, method, (int)speed);
    UpdateInfo();
}

void CSpeedometr::UpdateInfo()
{
    if( CSpeedometr::thiz == nullptr ) return;

    JNIEnv* env = g_pJavaWrapper->GetEnv();

    CPedSamp *pPed = CLocalPlayer::GetPlayerPed();
    if(!pPed) return;
    if(!pPed->m_pPed->IsInVehicle()) return;

    CVehicleSamp* pVehicle = pPed->GetCurrentVehicle();

    auto method = env->GetMethodID(clazz, "updateInfo", "(IIIIIII)V");
    env->CallVoidMethod(CSpeedometr::thiz,method,
                        (int) CSpeedometr::fFuel,
                        (int) pVehicle->GetHealth(),
                        CSpeedometr::iMilliage,
                        pVehicle->m_pVehicle->m_nVehicleFlags.bEngineOn,
                        (int) pVehicle->m_pVehicle->GetLightsStatus(),
                        (int) pVehicle->m_pVehicle->m_nDoorLock == CARLOCK_LOCKED,
                        pVehicle->m_iTurnState
    );
}

void CSpeedometr::updateFuel() {
    if( !CSpeedometr::bIsShow ) return;
    JNIEnv* env = g_pJavaWrapper->GetEnv();

    CPedSamp *pPed = CLocalPlayer::GetPlayerPed();
    if(!pPed) return;
    if(!pPed->m_pPed->IsInVehicle()) return;

    jmethodID method = env->GetMethodID(clazz, "updateFuel", "(I)V");
    env->CallVoidMethod(
            CSpeedometr::thiz,
            method,
            (int)CSpeedometr::fFuel
    );
}

void CSpeedometr::setMaxCarValues(int maxHp, int maxTank) {
    if( !CSpeedometr::bIsShow ) return;
    Log("CSpeedometr::setMaxCarValues()");

    JNIEnv* env = g_pJavaWrapper->GetEnv();

    jmethodID method = env->GetMethodID(clazz, "setMaxCarValues", "(II)V");
    env->CallVoidMethod(
            CSpeedometr::thiz,
            method,
            maxHp,
            maxTank
    );
}

void CSpeedometr::updateTurn(bool isTurn) {
    if(CSpeedometr::thiz == nullptr) return;

    JNIEnv* env = g_pJavaWrapper->GetEnv();

    CPedSamp *pPed = CLocalPlayer::GetPlayerPed();
    if(!pPed) return;
    if(!pPed->m_pPed->IsInVehicle()) return;

    CVehicleSamp* pVehicle = pPed->GetCurrentVehicle();

    jmethodID method = env->GetMethodID(clazz, "updateTurn", "(ZI)V");
    env->CallVoidMethod(
            CSpeedometr::thiz,
            method,
            isTurn,
            pVehicle->m_iTurnState
    );
}

extern "C"
JNIEXPORT void JNICALL
Java_com_blrp_game_gui_Speedometer_sendClick(JNIEnv *env, jobject thiz, jint click_id) {
    switch(click_id)
    {
        case CSpeedometr::BUTTON_ENGINE:
        {
            pNetGame->SendChatCommand("/en");
            CSpeedometr::UpdateInfo();
            break;
        }
        case CSpeedometr::BUTTON_LIGHT:
        {
            pNetGame->SendChatCommand("/light");
            CSpeedometr::UpdateInfo();
            break;
        }
        case CSpeedometr::BUTTON_TURN_LEFT:
        {
            CPedSamp *pPlayerPed = CLocalPlayer::m_pPlayerPed;
            CVehicleSamp* pVehicle = pPlayerPed->GetCurrentVehicle();

            if(pVehicle->m_iTurnState == eTurnState::TURN_LEFT)
                pVehicle->m_iTurnState = eTurnState::TURN_OFF;
            else
                pVehicle->m_iTurnState = eTurnState::TURN_LEFT;
            CSpeedometr::UpdateInfo();
            break;
        }
        case CSpeedometr::BUTTON_TURN_RIGHT:
        {
            CPedSamp *pPlayerPed = CLocalPlayer::m_pPlayerPed;
            CVehicleSamp* pVehicle = pPlayerPed->GetCurrentVehicle();

            if(pVehicle->m_iTurnState == eTurnState::TURN_RIGHT)
                pVehicle->m_iTurnState = eTurnState::TURN_OFF;
            else
                pVehicle->m_iTurnState = eTurnState::TURN_RIGHT;
            CSpeedometr::UpdateInfo();
            break;
        }
        case CSpeedometr::BUTTON_TURN_ALL:
        {
            CPedSamp *pPlayerPed = CLocalPlayer::m_pPlayerPed;
            CVehicleSamp* pVehicle = pPlayerPed->GetCurrentVehicle();

            if(pVehicle->m_iTurnState == eTurnState::TURN_ALL)
                pVehicle->m_iTurnState = eTurnState::TURN_OFF;
            else
                pVehicle->m_iTurnState = eTurnState::TURN_ALL;
            CSpeedometr::UpdateInfo();
            break;
        }
    }
}

extern "C"
JNIEXPORT jint JNICALL
        Java_com_blrp_game_gui_Speedometer_nativeGetMaxSpeed(JNIEnv *env, jobject thiz) {
return 300;
}
