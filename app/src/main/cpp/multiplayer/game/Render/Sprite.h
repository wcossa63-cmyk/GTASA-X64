//
// Created on 02.05.2023.
//

#pragma once

#include "../RW/RenderWare.h"
#include "game/common.h"

class CSprite {
public:
    static float m_f2DNearScreenZ;
    static float m_f2DFarScreenZ;
    static float m_fRecipNearClipPlane;

public:
    static void InjectHooks();

    static void Initialise();
    static void InitSpriteBuffer();
    static void InitSpriteBuffer2D();
    static void FlushSpriteBuffer();

    static void RenderBufferedOneXLUSprite2D(float posX, float posY, float sizeX, float sizeY, const CRGBA* color, int16 intensity, uint8 alpha);
    static void RenderBufferedOneXLUSprite(float ScreenX, float ScreenY, float ScreenZ, float SizeX, float SizeY, uint8 r, uint8 g, uint8 b, int16 intensity, float recipNearZ, uint8 a11);
    static void RenderOneXLUSprite(CVector pos, float SizeX, float SizeY, uint8 r, uint8 g, uint8 b, int16 intensity, float rhw, uint8 a, uint8 udir, uint8 vdir);
    static void RenderBufferedOneXLUSprite_Rotate_Dimension(float x, float y, float z, float SizeX, float SizeY, uint8 r, uint8 g, uint8 b, int16 intensity, float rz, float rotation, uint8 a);
    static void Draw3DSprite(float, float, float, float, float, float, float, float, float);

    static bool  CalcScreenCoors(const RwV3d& posn, RwV3d* out, float* w, float* h, bool checkMaxVisible, bool checkMinVisible);
    static float CalcHorizonCoors();
    static bool
    NewCalcScreenCoors(const RwV3d *In, RwV3d *pResult, float *pScaleX, float *pScaleY,
                    bool ClipFarPlane,
                    bool ClipNearPlane);
    static void
    RenderOneXLUSprite(float ScreenX, float ScreenY, float ScreenZ, float SizeX, float SizeY,
                       uint8_t R,
                       uint8_t G, uint8_t B, uint16_t Intensity16, float RecipZ, uint8_t Alpha,
                       bool FlipU, bool FlipV, float uvPad1, float uvPad2);

};
