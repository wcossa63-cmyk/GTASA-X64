#include "main.h"
#include "CSettings.h"
#include "game/game.h"
#include "vendor/ini/config.h"
#include "CDebugInfo.h"

stSettings CSettings::m_Settings;

static void ClearBackslashN(char *pStr, size_t size) {
	for (size_t i = 0; i < size; i++) {
		if (pStr[i] == '\n' || pStr[i] == 13)
		{
			pStr[i] = 0;
		}
	}
}

void CSettings::save()
{
	char buff[0x7F];
	sprintf(buff, "%sSAMP/settings.ini", g_pszStorage);
	remove(buff);

	ini_table_s *config = ini_table_create();

	ini_table_create_entry(config, "client", "name", m_Settings.szNickName);
	ini_table_create_entry(config, "client", "password", m_Settings.szPassword);
	ini_table_create_entry(config, "client", "player_password", m_Settings.player_password);
	
	ini_table_create_entry_as_int(config, "client", "autologin", m_Settings.szAutoLogin);

	ini_table_create_entry_as_int(config, "gui", "hparmourtext", m_Settings.iHPArmourText);
	ini_table_create_entry_as_int(config, "gui", "damageinformer", m_Settings.iIsEnableDamageInformer);
	ini_table_create_entry_as_int(config, "gui", "text3dinveh", m_Settings.iIsEnable3dTextInVehicle);

	ini_table_create_entry_as_int(config, "client", "server", m_Settings.szServer);
	ini_table_create_entry_as_int(config, "client", "debug", m_Settings.szDebug);
	ini_table_create_entry_as_int(config, "client", "headmove", m_Settings.szHeadMove);
	ini_table_create_entry_as_int(config, "client", "dl", m_Settings.szDL);
	ini_table_create_entry_as_int(config, "client", "timestamp", m_Settings.szTimeStamp);
	ini_table_create_entry_as_int(config, "client", "test", m_Settings.isTestMode);

	ini_table_create_entry(config, "gui", "Font", m_Settings.szFont);

	ini_table_create_entry_as_float(config, "gui", "FontSize", m_Settings.fFontSize);
	ini_table_create_entry_as_int(config, "gui", "FontOutline", m_Settings.iFontOutline);

	ini_table_create_entry_as_int(config, "gui", "fps", m_Settings.iFPS);

	ini_table_create_entry_as_int(config, "gui", "ChatFontSize", m_Settings.iChatFontSize);
	ini_table_create_entry_as_int(config, "gui", "ChatMaxMessages", m_Settings.iChatMaxMessages);
	ini_table_create_entry_as_int(config, "gui", "androidKeyboard", m_Settings.iAndroidKeyboard);
	ini_table_create_entry_as_int(config, "gui", "outfit", m_Settings.iOutfitGuns);

	ini_table_write_to_file(config, buff);
	ini_table_destroy(config);
}

extern bool g_bIsTestMode;
extern void ApplyFPSPatch(uint8_t fps);
void CSettings::LoadSettings(const char *szNickName, int iChatLines)
{
	char tempNick[40];
	if (szNickName)
	{
		strcpy(tempNick, szNickName);
	}

	Log("Loading settings..");

	char buff[0x7F];
	sprintf(buff, "%sSAMP/settings.ini", g_pszStorage);

	ini_table_s *config = ini_table_create();
	Log("Opening settings: %s", buff);
	if (!ini_table_read_from_file(config, buff))
	{
		Log("Cannot load settings, exiting...");
		CGame::exitGame();
		return;
	}

	snprintf(m_Settings.szNickName, sizeof(m_Settings.szNickName), "__android_%d%d", rand() % 1000, rand() % 1000);
	memset(m_Settings.szPassword, 0, sizeof(m_Settings.szPassword));
	memset(m_Settings.player_password, 0, sizeof(m_Settings.player_password));

	snprintf(m_Settings.szFont, sizeof(m_Settings.szFont), "visby-round-cf-extra-bold.ttf");

	std::string szName = ini_table_get_entry(config, "client", "name");
	const char *szPassword = ini_table_get_entry(config, "client", "password");
	const char *pPassword = ini_table_get_entry(config, "client", "player_password");

	m_Settings.szAutoLogin = ini_table_get_entry_as_int(config, "client", "autologin", 0);
	m_Settings.szServer = ini_table_get_entry_as_int(config, "client", "server", 0);

	m_Settings.szDebug = ini_table_get_entry_as_int(config, "client", "debug", 0);
	CDebugInfo::SetDrawFPS(CSettings::m_Settings.szDebug);

	m_Settings.szHeadMove = ini_table_get_entry_as_int(config, "client", "headmove", 0);
	m_Settings.szDL = ini_table_get_entry_as_int(config, "client", "dl", 0);
	m_Settings.szTimeStamp = ini_table_get_entry_as_int(config, "client", "timestamp", 0);
	m_Settings.isTestMode = ini_table_get_entry_as_int(config, "client", "test", 0);
	g_bIsTestMode = (bool)m_Settings.isTestMode;

	std::string szFontName = ini_table_get_entry(config, "gui", "Font");

	if(pPassword)
	{
		strcpy(m_Settings.player_password, pPassword);
	}
	if ( !szName.empty() )
	{
		strcpy(m_Settings.szNickName, szName.c_str());
	}
	if (szPassword)
	{
		strcpy(m_Settings.szPassword, szPassword);
	}
	if ( !szFontName.empty() )
	{
		strcpy(m_Settings.szFont, szFontName.c_str());
	}

	ClearBackslashN(m_Settings.szNickName, sizeof(m_Settings.szNickName));
	ClearBackslashN(m_Settings.szPassword, sizeof(m_Settings.szPassword));
	ClearBackslashN(m_Settings.szFont, sizeof(m_Settings.szFont));
	ClearBackslashN(m_Settings.player_password, sizeof(m_Settings.player_password));

	if (szNickName)
	{
		strcpy(m_Settings.szNickName, tempNick);
	}

	m_Settings.fFontSize = ini_table_get_entry_as_float(config, "gui", "FontSize", 30.0f);
	m_Settings.iChatFontSize = ini_table_get_entry_as_int(config, "gui", "ChatFontSize", -1);
	m_Settings.iFontOutline = ini_table_get_entry_as_int(config, "gui", "FontOutline", 2);

	m_Settings.iChatMaxMessages = ini_table_get_entry_as_int(config, "gui", "ChatMaxMessages", -1);

	m_Settings.iFPS = ini_table_get_entry_as_int(config, "gui", "fps", 60);
	if( m_Settings.iFPS < 20 ) m_Settings.iFPS = 60;
	ApplyFPSPatch(m_Settings.iFPS);

	m_Settings.iAndroidKeyboard = ini_table_get_entry_as_int(config, "gui", "androidKeyboard", 0);

	m_Settings.iOutfitGuns = ini_table_get_entry_as_int(config, "gui", "outfit", 1);
	CWeaponsOutFit::SetEnabled(CSettings::m_Settings.iOutfitGuns);

	m_Settings.iIsEnableDamageInformer = ini_table_get_entry_as_int(config, "gui", "damageinformer", 1);
	m_Settings.iIsEnable3dTextInVehicle = ini_table_get_entry_as_int(config, "gui", "text3dinveh", 1);

	m_Settings.iHPArmourText = ini_table_get_entry_as_int(config, "gui", "hparmourtext", 0);

	ini_table_destroy(config);
}