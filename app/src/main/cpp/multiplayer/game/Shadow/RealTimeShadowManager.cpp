#include "RealTimeShadowManager.h"
#include "util/patch.h"
CRealTimeShadowManager* pRealTimeShadowManager = nullptr;
void CRealTimeShadowManager::ReturnRealTimeShadow(CRealTimeShadow *shdw) {
    if (shdw->m_pOwner) {
        shdw->m_pOwner->m_pShadowData = nullptr;
        shdw->m_pOwner = nullptr;
    }
}

CRealTimeShadow* CRealTimeShadowManager::GetRealTimeShadow(CPhysical* physical) {
    return CHook::CallFunction<CRealTimeShadow*>(g_libGTASA + (VER_x32 ? 0x5B87AC + 1 : 0x6DD0C4), this, physical);
}

void CRealTimeShadowManager::DoShadowThisFrame(CPhysical* physical) {
    if (const auto shdw = physical->m_pShadowData) {

        if (shdw->m_pOwner != physical) {
            ReturnRealTimeShadow(shdw);
            physical->m_pShadowData = nullptr;
            (void)GetRealTimeShadow(physical);
            return;
        }
        shdw->m_bKeepAlive = true;
    } else {
        (void)GetRealTimeShadow(physical);
    }
}

inline void ReturnRealTimeShadow_hook(CRealTimeShadowManager* thiz, CRealTimeShadow *shdw) {
    thiz->ReturnRealTimeShadow(shdw);
}

inline void DoShadowThisFrame_hook(CRealTimeShadowManager* thiz, CPhysical *physical) {
    thiz->DoShadowThisFrame(physical);
}

void CRealTimeShadowManager::InjectHooks() {
    CHook::Redirect("_ZN22CRealTimeShadowManager17DoShadowThisFrameEP9CPhysical", &DoShadowThisFrame_hook);
    CHook::Redirect("_ZN22CRealTimeShadowManager20ReturnRealTimeShadowEP15CRealTimeShadow", &ReturnRealTimeShadow_hook);
}
