#pragma once

#include <jni.h>

#include <string>

class CJavaWrapper
{
	jmethodID j_Vibrate;
	jmethodID s_setPauseState;
	jmethodID s_ExitGame;
	jmethodID s_showNotification;

public:
    static JNIEnv* GetEnv();

    static void* NVThreadSpawnProc(void* arg);

	void SetPauseState(bool a1);

	int RegisterSexMale;
	int RegisterSkinValue;
	int RegisterSkinId;

	CJavaWrapper(JNIEnv* env, jobject activity);
	~CJavaWrapper();

	void ExitGame();

	void Vibrate(int milliseconds);

    void SendBuffer(const std::string& text) const;
    void OpenUrl(const std::string& url) const;

	jobject activity;

    void hideLoadingScreen();

    void CheckSignature();
	// -------- NEW -------- //
	void ShowNotification(int type, char *text, int duration, char *actionforBtn, char *textBtn);
};

extern CJavaWrapper* g_pJavaWrapper;
