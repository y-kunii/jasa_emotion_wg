/////////////////////////////////////////////////
// 
// JASA EmotionWG 展示会用デモ プログラムファイル
// emotionWG_rapiro_demo.c
// 
/////////////////////////////////////////////////
#include <Servo.h>
#include <Wire.h>
#include <avr/pgmspace.h>
#include "emotionWG_rapiro_demo.h"

/////////////////////////////////////////////////
// 変数
/////////////////////////////////////////////////

////////////
// 動作用
////////////
Servo servo[MAXSN];
uint8_t eyes[3] = { 0, 0, 0};

// Fine angle adjustments (degrees)
int trim[MAXSN]             = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};  // Initialize array to 0
int nowAngle[MAXSN]         = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};  // Initialize array to 0
int targetAngle[MAXSN]      = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};  // Initialize array to 0
int deltaAngle[MAXSN]       = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};  // Initialize array to 0
uint8_t bufferAngle[MAXSN]  = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};  // Initialize array to 0
uint8_t tempAngle[MAXSN]    = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};  // Initialize array to 0

int nowBright[3]            = { 0, 0, 0};  // Initialize array to 0
int targetBright[3]         = { 0, 0, 0};  // Initialize array to 0
int deltaBright[3]          = { 0, 0, 0};  // Initialize array to 0
uint8_t bufferBright[3]     = { 0, 0, 0};  // Initialize array to 0
uint8_t tempBright[3]       = { 0, 0, 0};  // Initialize array to 0

double  startTime           = 0;                // Motion start time(msec)
double  endTime             = 0;                // Motion end time(msec)
int     remainingTime       = 0;                // Motion remaining time(msec)
uint8_t bufferTime          = 0;                // Motion buffer time (0.1sec)

uint8_t motionNumber        = 0;
uint8_t frameNumber         = 0;
char    mode = 'M';
uint32_t scenarioTime       = (10000);          // シナリオ停止予定時間
boolean scenarioFlag        = false;            // シナリオ中フラグ


////////////
// 表情用
////////////

uint32_t mojiCount      = 0;
double   eyeBlinkTime     = 0;
uint8_t eysBlinkSts             = BLINK_INIT;

// 表示パターン用バッファ(R16xC8)
uint8_t left[8]  = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
uint8_t right[8]  = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
//uint8_t left[8]   = {0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF};
//uint8_t right[8]  = {0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF};


uint32_t testTime = 15000;

/////////////////////////////////////////////////
// プログラム
/////////////////////////////////////////////////

/**
 * @fn      ht_system_Seup
 * @brief   SystemSetup Registerの設定(システムオシレータのモード設定)
 * @param   [in] addr       デバイスアドレス
 * @param   [in] p          設定値
 * @return  なし
 */
void ht_system_Seup(uint8_t addr, uint8_t p){
    Wire.beginTransmission(addr);
    Wire.write(HT_CMD_SYSSET | p);
    Wire.endTransmission();
}

/**
 * @fn      ht_setBrightness
 * @brief   明るさの設定
 * @param   [in] addr       デバイスアドレス
 * @param   [in] p          設定値（1～15）
 * @return  なし
 */
void ht_setBrightness(uint8_t addr, uint8_t p){
    
    if (p > 15){
        p = 15;
    }
    Wire.beginTransmission(addr);
    Wire.write(HT_CMD_BRIGHTNESS | p);
    Wire.endTransmission();
}

/**
 * @fn      ht_display_setup
 * @brief   Display setup(点滅周期の設定)
 * @param   [in] addr       デバイスアドレス
 * @param   [in] p          設定値（0～3）
 * @return  なし
 */
void ht_display_setup(uint8_t addr, uint8_t p){
    
    if (p > 3){
        p = 0;
    }
    Wire.beginTransmission(addr);
    Wire.write(HT_CMD_BLINK | HT_BLINK_DISPLAY_ON | p);
    Wire.endTransmission();
}

/**
 * @fn      buffClear
 * @brief   表情用バッファクリア
 * @param   なし
 * @return  なし
 */
void buffClear(void){
    int i;
    for(i=0; i<8;i++){
        left[i] = 0;
        right[i] = 0;
    }
}

/**
 * @fn      ht_write
 * @brief   表示パターンの送信
 * @param   [in] pLeft      左側表示用バッファアドレス
 * @param   [in] pRight     右側表示用バッファアドレス
 * @return  なし
 */
void ht_write(unsigned char *pLeft, unsigned char *pRight) {
    uint8_t left;
    uint8_t right;
    
    // HT16K33 にデータ転送
    Wire.beginTransmission(HT_I2C_ADDRESS);
    Wire.write(HT_CMS_DATA);
    for (uint8_t i = 0; i < 8; i++){
        left = *pLeft++;
        right = *pRight++;
        
        Wire.write( left );
        Wire.write( right );
    }
    Wire.endTransmission();
}

/**
 * @fn      eyes_init
 * @brief   表情処理の初期化（HT16K33(LEDドライバ)）の初期化
 * @param   なし
 * @return  なし
 */
void eyes_init(void) {
    Wire.begin();
    
    ht_system_Seup(HT_I2C_ADDRESS, HT_SYSSET_OSC_ON);
    ht_display_setup(HT_I2C_ADDRESS, HT_BLINK_OFF);
    ht_setBrightness(HT_I2C_ADDRESS, 1);
}

/**
 * @fn      titleLogoScroll
 * @brief   タイトルロゴスクロール処理（setupでコール用）
 * @param   なし
 * @return  なし
 */
void titleLogoScroll(void){
    int i,j=0;
    mojiCount = 0;
    
    while(1){
        
        j = mojiCount;      // Titleの先頭を決定
        
        for(i=0;i<8;i++){
            if(j >= (TITLE_LOGO_MOJI_NUM * 8)){
                left[i] = 0x00;                         // テーブル数より多い場合は無点灯データをセット
            }
            else{
                left[i] = pgm_read_byte(&jasaStr[j++]);    // テーブルから左用のデータ8Byteを取得
            }
        }
        for(i=0;i<8;i++){
            if(j >= (TITLE_LOGO_MOJI_NUM * 8)){
                right[i] = 0x00;                        // テーブル数より多い場合は無点灯データをセット
            }
            else{
                right[i] = pgm_read_byte(&jasaStr[j++]);   // テーブルから右用のデータ8Byteを取得
            }
        }
        
        ht_write( left, right);                         // LED表示
        
        delay(SCROLL_DELAY);                            // Delay
        
        mojiCount++;                                    // テーブル先頭をずらす（scroll処理）
        if(mojiCount > (TITLE_LOGO_MOJI_NUM * 8 + 16) ){
            break;                                      // 文字がスクロースしきったらループを抜ける
        }
    }
}

/**
 * @fn      servo_init
 * @brief   姿勢制御の初期化
 * @param   なし
 * @return  なし
 */
void servo_init(void){
    int i;
    
    servo[0].attach(10);   // Head yaw
    servo[1].attach(11);   // Waist yaw
    servo[2].attach(9);    // R Sholder roll
    servo[3].attach(8);    // R Sholder pitch
    servo[4].attach(7);    // R Hand grip
    servo[5].attach(12);   // L Sholder roll
    servo[6].attach(13);   // L Sholder pitch
    servo[7].attach(14);   // L Hand grip
    servo[8].attach(4);    // R Foot yaw
    servo[9].attach(2);    // R Foot pitch
    servo[10].attach(15);  // L Foot yaw
    servo[11].attach(16);  // L Foot pitch
    eyes[R] = 6;           // Red LED of eyes
    eyes[G] = 5;           // Green LED of eyes
    eyes[B] = 3;           // Blue LED of eyes
    
    for( i = 0; i < MAXSN; i++) {
        targetAngle[i] = pgm_read_byte( &motion[0][0][i] ) << SHIFT;
        nowAngle[i] = targetAngle[i];
        servo[i].write((nowAngle[i] >> SHIFT) + trim[i]);
    }
    for(i = 0; i < 3; i++) {
        targetBright[i] = 0 << SHIFT;
        nowBright[i] = targetBright[i];
        analogWrite(eyes[i], nowBright[i] >> SHIFT);
    }
    
    delay(500);
    
    pinMode(POWER, OUTPUT);
    digitalWrite(POWER, HIGH);
}


/**
 * @fn      nextFrame
 * @brief   次の動作フレーム決定
 * @param   なし
 * @return  なし
 */
void nextFrame(void) {
    int i;
    frameNumber++;
    if(frameNumber >= MAXFN) {
        frameNumber = 0;
    }
    for(i = 0; i < MAXSN; i++) {
        bufferAngle[i] = pgm_read_byte( &motion[motionNumber][frameNumber][i] );
    }
    for( i = 0; i < 3; i++) {
        bufferBright[i] = pgm_read_byte( &motion[motionNumber][frameNumber][MAXSN+i] );
    }
    bufferTime = pgm_read_byte( &motion[motionNumber][frameNumber][TIME] );
    
    nextPose();
}

/**
 * @fn      nextPose
 * @brief   次のポーズ決定
 * @param   なし
 * @return  なし
 */
int nextPose() {
    int i;
    if(bufferTime > 0) {
        for(i = 0; i < MAXSN; i++) {
            targetAngle[i] = bufferAngle[i] << SHIFT;
            deltaAngle[i] = ((bufferAngle[i] << SHIFT) - nowAngle[i]) / (bufferTime * 10);
        }
        for(i = 0; i < 3; i++) {
            targetBright[i] = bufferBright[i] << SHIFT;
            deltaBright[i] = ((bufferBright[i] << SHIFT) - nowBright[i]) / (bufferTime * 10);
        }
    } else {
        for(i = 0; i < MAXSN; i++) {
            deltaAngle[i] = 0;
        }
        for(i = 0; i < 3; i++) {
            deltaBright[i] = 0;
        }
    }
    startTime = millis();
    endTime = startTime + (bufferTime * 100);
    bufferTime = 0;
}

//Read ASCII One-digit
int readOneDigit() {
    int buf;
    while(!Serial.available()) {}
    buf = Serial.read() - 48;
    if(buf < 0 || 9 < buf){
        buf = ERR;
    }
    return buf;
}

/**
 * @fn      servoMain
 * @brief   サーボ制御メイン処理
 * @param   なし
 * @return  なし
 */
void servoMain(void){
    int buf = ERR;
    int i;
    uint32_t suspendTime;
    
    if(Serial.available()) {
        if(Serial.read() == '#') {
            while(!Serial.available()){}
            switch(Serial.read()) {
            case 'M':
                digitalWrite(POWER, HIGH);
                buf = readOneDigit();
                if(buf != ERR){
                    motionNumber = buf;
                    mode = 'M';
                    digitalWrite(POWER, HIGH);
                    Serial.print("#M");
                    Serial.print(motionNumber);
                } else {
                    Serial.print("#EM");
                }
                scenarioTime = millis() + SCENARIO_MAX_TIME;
                scenarioFlag = true;
                Serial.println("Scenario Start");
                
                break;
            case 'N':
                digitalWrite(POWER, HIGH);
                buf = readOneDigit();
                if(buf != ERR){
                    motionNumber = buf+10;
                    mode = 'M';
                    digitalWrite(POWER, HIGH);
                    Serial.print("#M");
                    Serial.print(motionNumber);
                } else {
                    Serial.print("#EM");
                }
                scenarioTime = millis() + SCENARIO_MAX_TIME;
                scenarioFlag = true;
                Serial.println("Scenario Start");
                
                break;
            default:
                Serial.print("#E");
                break;
            }
        }
    }
    if( scenarioFlag == true){
        if(scenarioTime < millis()) {
            if( motionNumber != 5){
              motionNumber = 0;
            }
            scenarioFlag = false;
            Serial.println("Scenario Stop");
        }
    }
    if(endTime > millis()) {
        remainingTime = (endTime - millis()) / 10;
        for( i = 0; i < MAXSN; i++) {
            nowAngle[i] = targetAngle[i] - (deltaAngle[i] * remainingTime);
            servo[i].write((nowAngle[i] >> SHIFT) + trim[i]);
        }
        for( i = 0; i < 3; i++) {
            nowBright[i] = targetBright[i] - (deltaBright[i] * remainingTime);
            analogWrite(eyes[i], nowBright[i] >> SHIFT);
        }
    } else if(mode == 'M') {
        nextFrame();
    } else if(endTime + 500 < millis()){
        //digitalWrite(POWER, LOW);
    }
    suspendTime = scenarioTime + SERVO_SUSPEND_TIME;
    if( millis() > suspendTime){
        digitalWrite(POWER, LOW);                         // 一定時間動作していない場合、Servoの電源OFF
        Serial.println("suspend");
    }
}

/**
 * @fn      eyesBlink
 * @brief   まばたき判定
 * @param   なし
 * @return  なし
 */
uint8_t eyesBlink(void){
    uint8_t mNum = motionNumber;

    // 平常時または睡眠時
    if(motionNumber == 0 || motionNumber == 5){
        switch(eysBlinkSts){
        case BLINK_INIT:
            eyeBlinkTime = (millis() + EYES_BLINK_INTERVAL_TIME);
            eysBlinkSts = BLINK_WAIT;
            break;
        case BLINK_WAIT:
            if( eyeBlinkTime < millis()){
                if(motionNumber == 0){
                  mNum = 11;        // 平常時ならまばたき
                }else{
                  mNum = 12;        // 睡眠時ならZzz
                }
                eysBlinkSts = BLINK_OFF;
                eyeBlinkTime = millis() + EYES_BLINK_TIME;
            }
            break;
        case BLINK_OFF:
            if( eyeBlinkTime > millis()){
                if(motionNumber == 0){
                  mNum = 11;        // 平常時ならまばたき
                }else{
                  mNum = 12;        // 睡眠時ならZzz
                }
            }
            else if( eyeBlinkTime <= millis()){
                eysBlinkSts = BLINK_INIT;
            }
            break;
        }
    }
    return mNum;
}

/**
 * @fn      convertMotionToEye
 * @brief   動作番号を表情番号に変換
 * @param   なし
 * @return  なし
 */
uint8_t convertMotionToEye(uint8_t motionNo){
    uint8_t no;
    
    no = pgm_read_byte( &mortionToEye[motionNo] );
    return no;
}

/**
 * @fn      eyesMain
 * @brief   表情制御メイン処理
 * @param   なし
 * @return  なし
 */
void eyesMain(void){
    int i;
    uint8_t motionNum, eyeNum ;
    
    motionNum = eyesBlink();
    eyeNum = convertMotionToEye( motionNum );
    
    for(i=0;i<8;i++){
        left[i]  = pgm_read_byte( &eyesTbl[eyeNum][EYE_LEFT][i] );    // テーブルから左用のデータ8Byteを取得
        right[i] = pgm_read_byte( &eyesTbl[eyeNum][EYE_RIGHT][i] );   // テーブルから右用のデータ8Byteを取得
    }
    
    ht_write( left, right);                         // LED表示
}

void test(void)
{
    left[0]  = 0x01;
    left[1]  = 0x02;
    left[2]  = 0x04;
    left[3]  = 0x08;
    left[4]  = 0x10;
    left[5]  = 0x20;
    left[6]  = 0x40;
    left[7]  = 0x80;
    
    right[0] = 0x00;
    right[1] = 0x00;
    right[2] = 0x00;
    right[3] = 0x00;
    right[4] = 0x00;
    right[5] = 0x00;
    right[6] = 0x00;
    right[7] = 0x00;
    
    ht_write( left, right);                         // LED表示
}

/**
 * @fn      setup
 * @brief   arduino セットアップ処理
 * @param   なし
 * @return  なし
 */
void setup()  {
    eyes_init();
    servo_init();
    Serial.begin(57600);
    
    titleLogoScroll();
//    Serial.println("Reset!!");
}

/**
 * @fn      loop
 * @brief   arduino メイン処理
 * @param   なし
 * @return  なし
 */
void loop()  {
    servoMain();
    eyesMain();
//    test();
}
