#include "../main.h"
#include "../game/game.h"
#include "netgame.h"

void CObjectPool::Free()
{
    auto ids = GetAllIds();
    for (auto& id : ids) {
        Delete(id);
    }

    list.clear();
    entityToIdMap.clear();
    rwObjectToIdMap.clear();
}

bool CObjectPool::Delete(uint16_t objectId)
{
	auto pObject = GetAt(objectId);
	if(!pObject)
		return false;

	if (pObject->m_pEntity)
		entityToIdMap.erase(pObject->m_pEntity);

	delete pObject;
	list.erase(objectId);

	return true;
}

bool CObjectPool::New(uint16_t objectId, int iModel, CVector vecPos, CVector vecRot, float fDrawDistance)
{
	if(GetAt(objectId))
		Delete(objectId);

	auto pObject = CGame::NewObject(iModel, vecPos.x, vecPos.y, vecPos.z, vecRot, fDrawDistance);
	if (!pObject)
		return false;

	list[objectId] = pObject;
	if (pObject->m_pEntity)
		entityToIdMap[pObject->m_pEntity] = objectId;

	return true;
}

CObjectSamp *CObjectPool::GetObjectFromGtaPtr(CEntity *pGtaObject)
{
	const auto objectId = GetEntity(pGtaObject);
	return objectId == 0xFFFF ? nullptr : GetAt(objectId);
}

uint16_t CObjectPool::FindIDFromGtaPtr(CEntity* pGtaObject)
{
	const auto objectId = GetEntity(pGtaObject);
	return objectId == 0xFFFF ? INVALID_OBJECT_ID : static_cast<uint16_t>(objectId);
}

void CObjectPool::Process()
{
	static unsigned long s_ulongLastCall = 0;
	if (!s_ulongLastCall) s_ulongLastCall = GetTickCount();
	unsigned long ulongTick = GetTickCount();
	float fElapsedTime = ((float)(ulongTick - s_ulongLastCall)) / 1000.0f;
	// Get elapsed time in seconds

	for(auto &pair : list) {
		auto pObject = pair.second;
		auto pEntity = pObject->m_pEntity;

		pObject->Process(fElapsedTime);

		if (pEntity != pObject->m_pEntity) {
			if (pEntity)
				entityToIdMap.erase(pEntity);
			if (pObject->m_pEntity)
				entityToIdMap[pObject->m_pEntity] = pair.first;
		}
	}

	s_ulongLastCall = ulongTick;
}
