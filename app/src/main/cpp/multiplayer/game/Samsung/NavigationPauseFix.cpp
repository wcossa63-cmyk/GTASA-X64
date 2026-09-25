#include "NavigationPauseFix.h"

#include "../main.h"
#include "Timer.h"
#include <atomic>
#include <dlfcn.h>

static std::atomic_bool g_pauseRequested{false};
static std::atomic_bool g_pauseActive{false};

using MenuSwitchFn = void (*)();
using MenuPauseFn = void (*)(bool);
using MobileMenuInitFn = void (*)(void*);

static MenuSwitchFn g_menuSwitchOnFromGame = nullptr;
static MobileMenuInitFn g_mobileMenuInitForPause = nullptr;
static MenuSwitchFn g_menuMapInitPause = nullptr;
static MenuPauseFn g_menuPauseGame = nullptr;
static bool g_resolved = false;

static bool ResolveSymbols()
{
    if (g_resolved) {
        return g_menuSwitchOnFromGame && g_mobileMenuInitForPause && g_menuMapInitPause && g_menuPauseGame;
    }

    void* handle = dlopen("libGTASA.so", RTLD_NOW);
    if (handle) {
        g_menuSwitchOnFromGame = reinterpret_cast<MenuSwitchFn>(dlsym(handle, "_Z21Menu_SwitchOnFromGamev"));
        g_mobileMenuInitForPause = reinterpret_cast<MobileMenuInitFn>(dlsym(handle, "_ZN10MobileMenu12InitForPauseEv"));
        g_menuMapInitPause = reinterpret_cast<MenuSwitchFn>(dlsym(handle, "_Z17Menu_MapInitPausev"));
        g_menuPauseGame = reinterpret_cast<MenuPauseFn>(dlsym(handle, "_Z14Menu_PauseGameb"));
        dlclose(handle);
    }

    g_resolved = true;
    return g_menuSwitchOnFromGame && g_mobileMenuInitForPause && g_menuMapInitPause && g_menuPauseGame;
}

static void PauseGameFromNavigation()
{
    if (g_libGTASA == 0x00) {
        Log("PauseGameFromNavigation: libGTASA not resolved");
        return;
    }

    if (CTimer::GetIsPaused()) {
        g_pauseActive.store(true, std::memory_order_release);
        return;
    }

    if (g_pauseActive.exchange(true, std::memory_order_acq_rel)) {
        return;
    }

    if (!ResolveSymbols()) {
        Log("PauseGameFromNavigation: symbols missing (Switch=%p Init=%p Map=%p Pause=%p)",
             g_menuSwitchOnFromGame, g_mobileMenuInitForPause, g_menuMapInitPause, g_menuPauseGame);
        g_pauseActive.store(false, std::memory_order_release);
        return;
    }

    auto gMobileMenu = reinterpret_cast<void*>(g_libGTASA + 0x8BE780);

    g_menuPauseGame(true);
    g_menuMapInitPause();
    g_mobileMenuInitForPause(gMobileMenu);
    g_menuSwitchOnFromGame();

    const bool paused = CTimer::GetIsPaused();
    g_pauseActive.store(paused, std::memory_order_release);
}

void RequestNavigationPause()
{
    g_pauseRequested.store(true, std::memory_order_release);
}

void ProcessNavigationPause()
{
    if (g_pauseActive.load(std::memory_order_acquire) && !CTimer::GetIsPaused()) {
        g_pauseActive.store(false, std::memory_order_release);
    }

    if (!g_pauseRequested.exchange(false, std::memory_order_acq_rel)) {
        return;
    }
    PauseGameFromNavigation();
}

void NotifyMenuResumed()
{
    g_pauseActive.store(false, std::memory_order_release);
}
