//=====================================================================
//  Leafony Platform sample sketch
//     Platform     : BLE low power SL
//     Processor    : ATmega328P (3.3V /8MHz)
//     Application  : BLE 4-Sensers demo
//
//     Leaf configuration
//       (1) AC02 BLE Sugar
//       (2) AI01 4-Sensors
//       (3) AP01 AVR MCU
//       (4) AZ01 USB
//
//    (c) 2020 Trillion-Node Study Group
//    Released under the MIT license
//    https://opensource.org/licenses/MIT
//
//      Rev.00 2019/8/29  First release
//=====================================================================
//use libraries
//Adafruit LIS3DH
//https://github.com/adafruit/Adafruit_LIS3DH
//※  Adafruit_LIS3DH.h
//    uint8_t readRegister8(uint8_t reg);
//    void writeRegister8(uint8_t reg, uint8_t value);
//    をpublic:に移動する
//Adafruit Unified Sensor Driver
//https://github.com/adafruit/Adafruit_Sensor
//SmartEverything ST HTS221 Humidity Sensor
//https://github.com/ameltech/sme-hts221-library
//ClosedCube Arduino Library for ClosedCube OPT3001
//https://github.com/closedcube/ClosedCube_OPT3001_Arduino
//Blue gecko library for Trillion Node Engine AC02
//ST7032 - Arduino LiquidCrystal compatible library
//https://github.com/tomozh/arduino_ST7032
//=====================================================================

//=====================================================================
// difinition
//=====================================================================
#include <MsTimer2.h>
#include <avr/wdt.h>
#include <avr/sleep.h>
#include <avr/power.h>

#include <Wire.h>
#include <HTS221.h>
#include <ClosedCube_OPT3001.h>
#include "TBGLib.h"
#include <SoftwareSerial.h>
// #include <ST7032.h>

//=====================================================================
// BLE Local device name
// 最大16文字（ASCIIコード）まで
//=====================================================================
String strDeviceName = "Leafony_AC02";    // Unique Name
//=====================================================================

//=====================================================================
// SLEEP動作の有効無効切り替え
// SLEEP_ENABLE = 0 :無効 SLEEP_ENABLE = 1 :有効
//=====================================================================
#define SLEEP_ENABLE        (0)
//=====================================================================

//=====================================================================
// シリアルコンソールへのデバック出力
//      #define DEBUG = 出力あり
//    //#define DEBUG = 出力なし（コメントアウトする）
//=====================================================================
#define DEBUG
//=====================================================================

//=====================================================================
// スリープ時間、起動時間、送信間隔の設定
//  SLEEP_INTERVAL :スリープ時間　8秒単位で設定
//  WAKE_INTERVAL  ：起動時間（スリープ復帰からスリープまでの時間）1秒単位
//  SEND_INTERVAL  :送信間隔（センサーデータを送る間隔  1秒単位
//  Web Bluetoothを使用する場合はSLEEP_INTERVALとWAKE_INTERVALは変更不可
//=====================================================================
#define SLEEP_INTERVAL  (1)       // 8s x 1 = 8s
#define WAKE_INTERVAL   (5)       // 5s
#define SEND_INTERVAL   (1)       // 1s

//=====================================================================
// 出力先
//  #define  PC_APP     = Web Bluetooth
//=====================================================================
#define  PC_APP

//=====================================================================
// LCD出力
//  LCD = 1　出力　LCD= 0出力無し
//  LCD出力を行う場合はST7032.hのインクルードを有効にすること
//=====================================================================
#define LCD             (0)                 // LCD Leaf use:1


//=====================================================================
// IOピンの名前定義
// 接続するリーフに合わせて定義する
//=====================================================================
// --------------------------------------------
// PD port
//     digital 0: PD0 = PCRX    (HW UART)
//     digital 1: PD1 = PCTX    (HW UART)
//     digital 2: PD2 = INT0#
//     digital 3: PD3 = INT1#
//     digital 4: PD4 = RSV
//     digital 5: PD5 = CN3_D5
//     digital 6: PD6 = DISCN
//     digital 7: PD7 = BLSLP#
// --------------------------------------------
#define PCTX           0
#define PCRX           1
#define INT0           2
#define INT1           3
#define LED            4
#define PIN3_D5        5
#define BLE_RESET_PIN  6
#define BLE_WAKEUP_PIN 7

// --------------------------------------------
// PB port
//     digital 8: PB0 = UART2_RX (software UART)  /* not use */
//     digital 9: PB1 = UART2_TX (software UART)  /* not use */
//     digital 10:PB2 = SS#
//     digital 11:PB3 = MOSI
//     digital 12:PB4 = MISO
//     digital 13:PB5 = SCK (LED)
//                PB6 = XTAL1
//                PB7 = XTAL2
//---------------------------------------------
#define UART2_RX  8
#define UART2_TX  9
#define SS       10
#define MOSI     11
#define MISO     12
#define LED_PIN  13

// --------------------------------------------
// PC port
//     digital 14/ Analog0: PC0 = PIN24_D14
//     digital 15/ Analog1: PC1 = BLETX (software UART)
//     digital 16/ Analog2: PC2 = BLERX (software UART)
//     digital 17/ Analog3: PC3 = PIN27_D17
//     digital 18/ SDA    : PC4 = SDA   (I2C)
//     digital 19/ SCL    : PC5 = SCL   (I2C)
//     RESET              : PC6 = RESET#
//-----------------------------------------------
#define PIN24_D14 14
#define BLETX     15
#define BLERX     16
#define PIN27_D17 17
#define SDA       18
#define SCL       19

//=====================================================================
// プログラム内で使用する定数定義
//
//=====================================================================
//-----------------------------------------------
//３軸センサ、輝度センサ I2Cアドレス
//-----------------------------------------------
#define LIS2DH_ADDRESS 0x19       // SD0/SA0 pin = VCC
#define OPT3001_ADDRESS 0x45      // ADDR pin = VCC
#define I2C_EXPANDER_ADDR_LCD   0x1A

//-----------------------------------------------
// loop() interval
// MsTimer2のタイマー割り込み発生間隔(ms)
//-----------------------------------------------
#define LOOP_INTERVAL 125         // 125ms interval

//-----------------------------------------------
// BLE
//-----------------------------------------------
#define BLE_STATE_STANDBY               (0)
#define BLE_STATE_SCANNING              (1)
#define BLE_STATE_ADVERTISING           (2)
#define BLE_STATE_CONNECTING            (3)
#define BLE_STATE_CONNECTED_MASTER      (4)
#define BLE_STATE_CONNECTED_SLAVE       (5)

//-----------------------------------------------
// Batt ADC ADC081C027
//-----------------------------------------------
#define BATT_ADC_ADDR 0x50

//=====================================================================
// object
//=====================================================================

//-----------------------------------------------
// BLE
//-----------------------------------------------
SoftwareSerial Serialble(BLERX, BLETX);
BGLib ble112((HardwareSerial *)&Serialble, 0, 0 );

//-----------------------------------------------
// LCD
//-----------------------------------------------
#if LCD
  ST7032 lcd;
#endif



#define I2C_PIR_ADDR   0x65
#define I2C_SEND_BUF_LENGTH 10
#define I2C_RECEIVE_BUF_LENGTH 10

//=====================================================================
// プログラムで使用する変数定義
//
//=====================================================================
//=====================================================================
// RAM data
//=====================================================================
volatile int state = 0;
float datadist;
unsigned char i2c_sendBuf[I2C_SEND_BUF_LENGTH];
unsigned char i2c_receiveBuf[I2C_RECEIVE_BUF_LENGTH];
unsigned char i2c_receiveLenght;
byte readReg;
double irData;
double tempData;
char buf[120];


//---------------------------
// loop counter
//---------------------------
uint8_t iLoop1s = 0;
uint8_t iSleepCounter = 0;
uint8_t iSendCounter = 0;

//---------------------------
// event
//---------------------------
bool eventSensorRead = false;
bool eventBLEsendData = false;
bool eventSleepCheck = false;

//---------------------------
// interval Timer2 interrupt
//---------------------------
volatile bool bInterval = false;

//---------------------------
// BLE
//---------------------------
bool bBLEconnect = false;
bool bBLEsendData = false;
int8_t bleSendCount = 0;

volatile uint8_t ble_state = BLE_STATE_STANDBY;
volatile uint8_t ble_encrypted = 0;          // 0 = not encrypted, otherwise = encrypted
volatile uint8_t ble_bonding = 0xFF;         // 0xFF = no bonding, otherwise = bonding handle

//---------------------------
// LCD
//---------------------------
volatile bool  bLCDchange = false;
volatile int lcd_view_sts = 0;

//---------------------------
// Sleep, Watchdog Timer
//---------------------------
volatile bool bSleep = false;
volatile bool bBLESleep = false;

volatile int countWDT = 0;
volatile int wakeupWDT = SLEEP_INTERVAL;

volatile bool bWakeupINT0 = false;
volatile bool bWakeupINT1 = false;

//---------------------------
// led
//---------------------------
uint8_t iLed = 0;
volatile uint8_t iToggle = 8;

bool bToggle = 0;

//---------------------------
// Batt
//---------------------------
float dataBatt = 0;


//=====================================================================
// setup
//=====================================================================
//-----------------------------------------------
// port
//-----------------------------------------------
//=====================================================================
// IOピンの入出力設定
// 接続するリーフに合わせて設定する
//=====================================================================
void setupPort(){

  //---------------------
  // PD port
  //---------------------
  // PD0 : digital 0 = RX
  // PD1 : digital 1 = TX

  pinMode(INT0, INPUT);             // PD2 : digital 2 = BLE interrupt
  pinMode(INT1, INPUT);             // PD3 : digital 3 = sensor interrupt

  pinMode(LED, OUTPUT);             // PD4 : digital 4 = LED
  digitalWrite(LED, LOW);

  pinMode(PIN3_D5, INPUT);          // PD5 : digital 5 = not used

  pinMode(BLE_RESET_PIN, OUTPUT);   // PD6 : digital 6 = BLE reset active-low
  digitalWrite(BLE_RESET_PIN, LOW);

  pinMode(BLE_WAKEUP_PIN, OUTPUT);  // PD7 : digital 7 = BLE sleep
  digitalWrite(BLE_WAKEUP_PIN, HIGH);

  //---------------------
  // PB port
  //---------------------
  pinMode(UART2_RX, INPUT);     // PB0 : digital 8 = software UART2

  pinMode(UART2_TX, INPUT);     // PB1 : digital 9 = software UART2

  pinMode(SS, INPUT);           // PB2 : digital 10 = not used

  pinMode(MOSI, INPUT);         // PB3 : digital 11 = not used

  pinMode(MISO, INPUT);         // PB4 : digital 12 = not used

  pinMode(LED_PIN, OUTPUT);     // PB5 : digital 13 =LED on 8bit-Dev. Leaf
  digitalWrite(LED_PIN, LOW);

  //---------------------
  // PC port
  //---------------------
  pinMode(PIN24_D14, INPUT);     // PC0 : digital 14 = not used

  // PC1 : digital 15 = BLETX
  // PC2 : digital 16 = BLERX

  pinMode(PIN27_D17, INPUT);     // PC3 : digital 17  = not used

  // PC4 : digital 18 = I2C SDA
  // PC5 : digital 19 = I2C SCL
}
//=====================================================================
// 割り込み処理初期設定
//
//=====================================================================
//-----------------------------------------------
// external interrupt
// 外部割り込み設定
//-----------------------------------------------
void setupExtInt(){

  attachInterrupt(0, intExtInt0, FALLING);    // BLE    INT0# = enabled
  detachInterrupt(1);                         // sensor INT1# = disabled
}

//-----------------------------------------------
// timer2 interrupt (interval=125ms, int=overflow)
// メインループのタイマー割り込み設定
//-----------------------------------------------
void setupTC2Int(){

  MsTimer2::set(LOOP_INTERVAL, intTimer2);
}
//=====================================================================
// I2C　制御関数
//
//=====================================================================
//-----------------------------------------------
//I2C スレーブデバイスに1バイト書き込む
//-----------------------------------------------
void i2c_write_byte(int device_address, int reg_address, int write_data){
  Wire.beginTransmission(device_address);
  Wire.write(reg_address);
  Wire.write(write_data);
  Wire.endTransmission();
}
//-----------------------------------------------
//I2C スレーブデバイスから1バイト読み込む
//-----------------------------------------------
unsigned char i2c_read_byte(int device_address, int reg_address){

  int read_data = 0;

  Wire.beginTransmission(device_address);
  Wire.write(reg_address);
  Wire.endTransmission(false);

  Wire.requestFrom(device_address, 1);
  read_data = Wire.read();

  return read_data;
}
/**********************************************
* I2C スレーブデバイスに複数バイト書き込む
**********************************************/
void i2c_write(int device_address, int reg_address, int lengrh, unsigned char* write_byte){

  Wire.beginTransmission(device_address);
  Wire.write(reg_address);
  for (int i = 0; i < lengrh; i++){
    Wire.write(write_byte[i]);
  }
  Wire.endTransmission();
}
/**********************************************
* I2C スレーブデバイスから複数バイト読み込む
**********************************************/
void i2c_read(int device_address, int reg_address, int lengrh, unsigned char* read_byte){

  Wire.beginTransmission(device_address);
  Wire.write(reg_address);
  Wire.endTransmission(false);

  Wire.requestFrom(device_address, lengrh);
  for (int i = 0; i < lengrh; i++){
    read_byte[i] = Wire.read();
  }
}
/**********************************************
* I2C 受信バッファクリア
**********************************************/
void clearI2CReadbuf(){
  memcpy(i2c_receiveBuf, 0x00, I2C_RECEIVE_BUF_LENGTH);
}
//=====================================================================
// 各デバイスの初期設定
//
//=====================================================================


//-----------------------------------------------
// BLE
//-----------------------------------------------
void setupBLE(){

    String stWork;
    uint8  stLen;
    uint8 adv_data[31];

    // set up internal status handlers (these are technically optional)
    ble112.onBusy = onBusy;
    ble112.onIdle = onIdle;
    ble112.onTimeout = onTimeout;
    // ONLY enable these if you are using the <wakeup_pin> parameter in your firmware's hardware.xml file
    // BLE module must be woken up before sending any UART data

    // set up BGLib response handlers (called almost immediately after sending commands)
    // (these are also technicaly optional)

    // set up BGLib event handlers
    /* [gatt_server] */
    ble112.ble_evt_gatt_server_attribute_value = my_evt_gatt_server_attribute_value;    /* [BGLib] */
    /* [le_connection] */
    ble112.ble_evt_le_connection_opend = my_evt_le_connection_opend;                /* [BGLib] */
    ble112.ble_evt_le_connection_closed = my_evt_le_connection_closed;              /* [BGLib] */
    /* [system] */
    ble112.ble_evt_system_boot = my_evt_system_boot;                                /* [BGLib] */

    ble112.ble_evt_system_awake = my_evt_system_awake;
    ble112.ble_rsp_system_get_bt_address = my_rsp_system_get_bt_address;
    /*  */

    Serialble.begin(9600);
    // reset module (maybe not necessary for your application)
    //digitalWrite(BLE_RESET_PIN, LOW);
    //delay(5); // wait 5ms
    //digitalWrite(BLE_RESET_PIN, HIGH);

    /* setting */
    /* [set Advertising Data] */
    uint8 ad_data[21] = {
        (2),                                    // field length
        BGLIB_GAP_AD_TYPE_FLAGS,                // field type (0x01)
        (6),                                    // data
        (1),                                    // field length (1は仮の初期値)
        BGLIB_GAP_AD_TYPE_LOCALNAME_COMPLETE    // field type (0x09)
    };
    /*  */
    size_t lenStr2 = strDeviceName.length();
    ad_data[3] = (lenStr2 + 1);                     // field length
    uint8 u8Index;
    for( u8Index=0; u8Index < lenStr2; u8Index++)
    {
      ad_data[5 + u8Index] = strDeviceName.charAt(u8Index);
    }
    /*   */
    stLen = (5 + lenStr2);
    ble112.ble_cmd_le_gap_set_adv_data( SCAN_RSP_ADVERTISING_PACKETS, stLen, ad_data );
    while (ble112.checkActivity(1000));                 /* 受信チェック */

    /* interval_min :   40ms( =   64 x 0.625ms ) */
    /* interval_max : 1000ms( = 1600 x 0.625ms ) */
    ble112.ble_cmd_le_gap_set_adv_parameters( 64, 1600, 7 );    /* [BGLIB] <interval_min> <interval_max> <channel_map> */
    while (ble112.checkActivity(1000));                         /* [BGLIB] 受信チェック */

    /* start */
    //ble112.ble_cmd_le_gap_start_advertising(1, LE_GAP_GENERAL_DISCOVERABLE, LE_GAP_UNDIRECTED_CONNECTABLE);
    ble112.ble_cmd_le_gap_start_advertising( 0, LE_GAP_USER_DATA, LE_GAP_UNDIRECTED_CONNECTABLE );    // index = 0
    while (ble112.checkActivity(1000));                 /* 受信チェック */
    /*  */
}

//=====================================================================
// 割り込み処理
//
//=====================================================================
//=====================================================================
// interrupt
//=====================================================================
//=====================================================================
void catchHuman()
{
  state = 1;
  Serial.println("!! Interrupt !!");      //人の接近を検知
}
//=====================================================================
double clacIR()
{
  double ret;
  unsigned short val = (unsigned short)((i2c_receiveBuf[2] << 8) |  i2c_receiveBuf[1]);
  if ( (val & 0x8000) == 0x8000)
  {
    val = ~val + 1;
    ret = (double)(val *   0.4578 ) * -1;
  }
  else
  {
    ret = (double)(val *  0.4578 );
  }
  return ret;
}
//----------------------------------------------
// Timer2 INT
// タイマー割り込み関数
//----------------------------------------------
void intTimer2(){
  Serial.println("*Intarval*");
  bInterval = true;
}

//---------------------------------------------
// Watchdog Timer INT
// WDT割り込み関数
//---------------------------------------------
ISR(WDT_vect){

  wdt_disable();

  if (bSleep == true){
    countWDT += 1;

    if (countWDT >= wakeupWDT){
      countWDT = 0;
      bSleep = false;
    }
  }
}

//----------------------------------------------
// INT0
// INT0割り込み関数
//----------------------------------------------
void intExtInt0(){

  bSleep = false;
  //bWakeupINT0 = true;
}

//----------------------------------------------
// INT1
// INT1割り込み関数
//----------------------------------------------
void intExtInt1(){

  bSleep = false;
  //bWakeupINT1 = true;
}


//====================================================================
// functions
//====================================================================
//--------------------------------------------------------------------
// counter /event
//--------------------------------------------------------------------
//-----------------------------------------
// main loop
// メインループのループ回数をカウントし
// 1秒間隔でセンサーデータの取得とBLEの送信をONにする
// 4秒間隔でスリープ確認をONにする
//-----------------------------------------
void loopCounter(){

  iLoop1s += 1;

  //--------------------
  // 1s period
  //--------------------
  if (iLoop1s >=  8){             // 125ms x 8 = 1s

    iLoop1s = 0;

    iSleepCounter += 1;
    iSendCounter  += 1;
    if (iSendCounter >= SEND_INTERVAL){
      iSendCounter = 0;
      eventSensorRead = true;
      eventBLEsendData = true;
    }

    //-----------------------
    // WAKE TIME period
    //-----------------------
    if (iSleepCounter >= WAKE_INTERVAL){

      iSleepCounter = 0;
      if (SLEEP_ENABLE){
        eventSleepCheck = true;
      }
    }
  }
}

//--------------------------------------------------------------------
// sensor
//--------------------------------------------------------------------
//-----------------------------------------
// main loop
// センサーデータ取得がONのとき、各センサーからデータを取得
// コンソール出力がONのときシリアルに測定値と計算結果を出力する
//-----------------------------------------
void loopSensor(){
  double temp_mv;
  //---------------------------
  // 1s period
  //---------------------------
  if (eventSensorRead == 1){


    //-------------------------
    // ADC081C027（ADC)
    // 電池リーフ電池電圧取得
    //-------------------------
    uint8_t adcVal1 = 0;
    uint8_t adcVal2 = 0;

    Wire.beginTransmission(BATT_ADC_ADDR);
    Wire.write(0x00);
    Wire.endTransmission(false);
    Wire.requestFrom(BATT_ADC_ADDR,2);
    adcVal1 = Wire.read();
    adcVal2 = Wire.read();

    if (adcVal1 == 0xff && adcVal2 == 0xff) {
      //測定値がFFならバッテリリーフはつながっていない
      adcVal1 = adcVal2 = 0;
    }

    //電圧計算　ADC　* （(リファレンス電圧(3.3V)/ ADCの分解能(256)) * 分圧比（２倍））
    //dataBatt = (((adcVal1 << 4) | (adcVal2 >> 4)) * (3.3 / 256)) * 2 ;
    temp_mv = ((double)((adcVal1 << 4) | (adcVal2 >> 4)) * 3300 * 2) / 256;
    dataBatt = (float)(temp_mv / 1000);
  }

}
//debug ///
void getBattVal()
{
  uint8_t adcVal1 = 0;
  uint8_t adcVal2 = 0;

  Wire.beginTransmission(BATT_ADC_ADDR);
  Wire.write(0x00);
  Wire.endTransmission(false);
  Wire.requestFrom(BATT_ADC_ADDR, 2);
  adcVal1 = Wire.read();
  adcVal2 = Wire.read();

  if (adcVal1 == 0xff && adcVal2 == 0xff) {
    //測定値がFFならバッテリリーフはつながっていない
    adcVal1 = adcVal2 = 0;
  }

  //電圧計算　ADC　* （(リファレンス電圧(3.3V)/ ADCの分解能(256)) * 分圧比（２倍））
  //dataBatt = (((adcVal1 << 4) | (adcVal2 >> 4)) * (3.3 / 256)) * 2 ;
  double temp_mv = ((double)((adcVal1 << 4) | (adcVal2 >> 4)) * 3300 * 2) / 256;
  float batval = (float)(temp_mv / 1000);
  Serial.println(" V=" + String(batval));
}

//--------------------------------------------------------------------
// BLE
//--------------------------------------------------------------------
//-----------------------------------------
// main loop
// BLEからデータが送信されていたらデータを取得し、取得データに従い
// 処理を実施
//-----------------------------------------
void loopBleRcv( void ){
    // keep polling for new data from BLE
    ble112.checkActivity(0);                    /* 受信チェック */

    /*  */
    if (ble_state == BLE_STATE_STANDBY) {
        bBLEconnect = false;                /* [BLE] 接続状態 */
    } else if (ble_state == BLE_STATE_ADVERTISING) {
        bBLEconnect = false;                /* [BLE] 接続状態 */
    } else if (ble_state == BLE_STATE_CONNECTED_SLAVE) {
        /*  */
        bBLEconnect = true;                 /* [BLE] 接続状態 */
        /*  */
        if (!ble_encrypted) {
//            digitalWrite(LED_PIN, slice < 100 || (slice > 200 && slice < 300));
        } else {
//            digitalWrite(LED_PIN, slice < 100 || (slice > 200 && slice < 300) || (slice > 400 && slice < 500));
        }
    }
}


void loopBleSnd( void ){
    /*  */
    if( eventBLEsendData == true ){
        Serial.println("AAAAA");
        eventBLEsendData = false;
        //--------------------------------
        // send sensor data
        //--------------------------------
        if( bBLEsendData == true ){
            bt_sendData();
        }
    }
}


//---------------------------------------
// send sensor data
// センサーデータをセントラルに送る文字列に変換してBLEリーフへデータを送る
//---------------------------------------
void bt_sendData(){

  float value;
  char temp[7], humid[7], light[7], tilt[7],battVolt[7], pips[7];
  char code[4];
  char sendData[40];
  uint8 sendLen;


  Serial.println("*BLE send*");

  //-----------------------------------
  //センサーデータを文字列に変換
  //dtostrf(変換する数字,変換される文字数,小数点以下の桁数,変換した文字の格納先);
  //変換される文字数を-にすると変換される文字は左詰め、+なら右詰めとなる
  //-----------------------------------

  //-------------------------
  // Temperature (4Byte)
  //-------------------------
//  value = datadist;
  dtostrf(value,4,1,temp);
  dtostrf(value,4,1,humid);
  dtostrf(value,5,0,light);
  dtostrf(value,4,0,tilt);
  dtostrf(value,4,0,pips);
  Serial.println("*************");
  Serial.println(temp);
  Serial.println("*************");
  
  //-------------------------
  // Battery Voltage (4Byte)
  //-------------------------
  value = dataBatt;

  if (value >= 10){
   value = 9.99;
  }
  dtostrf(value, 4, 2, battVolt);


  // BLEデバイスへの送信データセット
#ifdef IPHONE_APP
  // 専用iphoneアプリでのデータ送信フォーマット
  //-----------------------------------
  // Send data
  //   mark "D" = rxData[0]
  //   temp = rxData[1][2][3][4]
  //   humid= rxData[5][6][7][8]
  //   light= rxData[9][10][11][12][13]
  //   tilt = rxData[14][15][16][17]
  //   <CR> = rxData[18]
  //   <LF> = rxData[19]
  //-----------------------------------
  String str = "D";                // 1s data 0x44
  str.toCharArray(code,2);
  //sendLen = sprintf(sendData, "%s%s%s%s%s\r\n", code, temp, humid, light, tilt);
  sendLen = sprintf(sendData, "D%04s%04s%05s%04s\r\n", temp, humid, light, tilt);
#else
  trim(temp);
  trim(humid);
  trim(light);
  trim(tilt);
  trim(battVolt);
  trim(pips);

#ifdef PC_APP
  // 汎用iphoneアプリでのデータ送信フォーマット
  // sendLen = sprintf(sendData, "T=%s, H=%s, L=%s, A=%s", temp, humid, light, tilt);
  // sendLen = sprintf(sendData, "T=%s,H=%s,L=%s,A=%s,V=%s", temp, humid, light, tilt, battVolt);
  // WebBluetoothアプリ用フォーマット
  sendLen = sprintf(sendData, "%04s,%04s,%04s,%04s,%04s,%01s\n", temp, humid, light, tilt, battVolt, pips);
  // sendLen = sprintf(sendData, "T=%s", temp);
#else
#if LCD
  lcd.clear();
#endif
  switch (bleSendCount)
  {
    case 0:
      //                 Tmp XX.X [degC]
      sendLen = sprintf(sendData, "Tmp %s [degC]", temp);
#if LCD
      lcd.print("Temp");
      lcd.setCursor(0, 1);
      lcd.print( String(temp) +" C");
      break;
#endif
    case 1:
      //                 Hum xx.x [%]
      sendLen = sprintf(sendData, "Hum %s [%%]", humid);
#if LCD
      lcd.print("Humidity");
      lcd.setCursor(0, 1);
      lcd.print( String(humid) +" %");
#endif
      break;
    case 2:
      //                 Lum XXXXX [lx]
      sendLen = sprintf(sendData, "Lum %s [lx]", light);
#if LCD
      lcd.print("Luminous");
      lcd.setCursor(0, 1);
      lcd.print( String(light) +" lx");
#endif
      break;
    case 3:
      //                 Ang XXXX [arc deg]
      sendLen = sprintf(sendData, "Ang %s [a deg]", tilt);
#if LCD
      lcd.print("Angle");
      lcd.setCursor(0, 1);
      lcd.print( String(tilt) +" deg");
#endif
      break;
    case 4:
      //                 Bat X.XX [V]
      sendLen = sprintf(sendData, "Bat %s [V]", battVolt);
#if LCD
      lcd.print("Battery");
      lcd.setCursor(0, 1);
      lcd.print( String(battVolt) +" V");
#endif
      break;
    default:
      break;
  }
  if (bleSendCount < 4){
    bleSendCount++;
  }
  else{
    bleSendCount = 0;
  }
#endif
#endif

  // BLEデバイスへの送信
  ble112.ble_cmd_gatt_server_send_characteristic_notification( 1, 0x000C, sendLen, (const uint8 *)sendData );
  while (ble112.checkActivity(1000));

#ifdef DEBUG
  Serial.println("###\tbt_sendDatasend: { Temp=" + String(temp) + ", Humid=" + String(humid) + ", Light=" + String(light) + ", Tilt=" + String(tilt) + ", Vbat=" + String(battVolt) + ", Dice=" + String(pips) + " }");
#endif
}
//---------------------------------------
// trim
// 文字列配列からSPを削除する
//---------------------------------------
void trim(char * data)
{
  int i = 0, j = 0;

  while (*(data + i) != '\0'){
    if (*(data + i) != ' '){
      *(data + j) = *(data + i);
      j++;
    }
    i++;
  }
  *(data + j) = '\0';
}

//--------------------------------------------------------------------
// sleep
//--------------------------------------------------------------------
//-----------------------------------------
// main loop
// スリープ移行要求があった場合、センサーリーフ、BLEリーフをSLEEPさせて
// WDTをセットしマイコンリーフをスリープさせる
//-----------------------------------------
void loopSleep(){

  if (eventSleepCheck == true){
    eventSleepCheck = false;

    //-----------------------
    // BLE connection
    //-----------------------
    if (bBLEconnect == false){

      bSleep = true;
      countWDT = 0;

#ifdef DEBUG
        Serial.print(F("  >>> Go to sleep :  "));
        Serial.println("wakeup after = " + String(wakeupWDT) + " x 8s  >>>");
        Serial.flush();
#endif

      //-----------------------
      // sensor sleep
      //-----------------------
      sleepSensor();

      //-----------------------
      // BLE sleep
      //-----------------------
      sleepBLE();

      //-----------------------
      // ATMega328 sleep
      //-----------------------
      while (bSleep == true){
        wdt_start();
        sleep();
      }

      //-----------------------
      // BLE wakeup
      //-----------------------
      wakeupBLE();

      //-----------------------
      // sensor wakeup
      //-----------------------
      wakeupSensor();

      //------------------------
      // resume
      //------------------------
#ifdef DEBUG
        Serial.println(F("  <<< Wake up <<<"));
#endif
    }
  }
}
//-----------------------------------------
// SLEEP
//-----------------------------------------
void sleep(){
  ADCSRA &= ~(1 << ADEN);                 //ADC停止
  PRR = 0x05;
  set_sleep_mode(SLEEP_MODE_PWR_DOWN);    //SET SLEEP MODE
  sleep_enable();                         // SLEEP ENABLE

  // BOD停止
  MCUCR |= (1 << BODSE) | (1 << BODS);             // MCUCRのBODSとBODSEに1をセット
  MCUCR = (MCUCR & ~(1 << BODSE)) | (1 << BODS);   // すぐに（4クロック以内）BODSSEを0, BODSを1に設定
  asm("sleep");                                    // 3クロック以内にスリープ
  sleep_disable();                         // SLEEP DISABLE
  PRR = 0x05;
}
//-----------------------------------------
// WDT
//-----------------------------------------
void wdt_start(){
  // watchdog timer reset
  wdt_reset();

  //disable interruput
  cli();
  //clear WatchDog system Reset Flag(WDRF)
  MCUSR &= ~(1 << WDRF);
  // WDT変更許可
  // WDCEとWDE同時セットで変更許可
  WDTCSR |= 1 << WDCE | 1 << WDE;
  //WDT設定
  // WDE=0,WDIE=1 :WDT overflowで割り込み
  // WDP3=1,WDP2=0,WDP1=0,WDP0=1: 8s
  WDTCSR = 1 << WDIE | 0 << WDE | 1 << WDP3 | 0 << WDP2 | 0 << WDP1 | 1 << WDP0;
  //enable interruput
  sei();
}
//-----------------------------------------
// sleep sensor
// センサーリーフをスリープさせる
//-----------------------------------------
void sleepSensor(){

}

//-----------------------------------------
// wakeup sensor
// センサーリーフをスリープから復帰させる
//-----------------------------------------
void wakeupSensor(){

}

//---------------------------------------
// sleep BLE
// BLE リーフをスリープさせる
//---------------------------------------
void sleepBLE(){

    ble112.ble_cmd_le_gap_stop_advertising( 1 );
    while (ble112.checkActivity());

    ble112.ble_cmd_system_halt( 1 );
    while (ble112.checkActivity());
    digitalWrite( BLE_WAKEUP_PIN, LOW );
    bBLESleep = true;
}

//---------------------------------------
// wakeup BLE
// BLEリーフをスリープから復帰させる
//---------------------------------------
void wakeupBLE(){
    uint8_t *last;

    digitalWrite( BLE_WAKEUP_PIN, HIGH );

    delay(10);

    while (1) {
        ble112.checkActivity();                             /* 受信チェック */
        last = ble112.getLastEvent();
        if (last[0] == 0x01 && last[1] == 0x04) break;      /* [evt_system_awake] [2]と[3] */
    }

#if 0
    Serial.println("");
    Serial.println("--- Wakeup BLE ---");
    Serial.println("");
#endif

   // ble112.ble_cmd_le_gap_set_adv_parameters( 320, 480, 7 );
   // while (ble112.checkActivity(1000));

    // put module into discoverable/connectable mode (with user-defined advertisement data)
    //ble112.ble_cmd_le_gap_set_mode( LE_GAP_GENERAL_DISCOVERABLE, LE_GAP_UNDIRECTED_CONNECTABLE );
    //ble112.ble_cmd_le_gap_start_advertising(0, LE_GAP_GENERAL_DISCOVERABLE, LE_GAP_UNDIRECTED_CONNECTABLE);
    ble112.ble_cmd_le_gap_start_advertising( 0, LE_GAP_USER_DATA, LE_GAP_UNDIRECTED_CONNECTABLE );    // index = 0
    while (ble112.checkActivity(1000));             /* 受信チェック(with timeout) */
}

//====================================================================
// setup
//====================================================================
void setup() {

  //WDT disable
  wdt_disable();
  delay(10);

  Serial.begin(115200);
  Wire.begin();             // I2C 100KHz
#ifdef DEBUG
    Serial.println(F("========================================="));
    Serial.println(F("setup start"));
#endif
#if LCD
 i2c_write_byte(I2C_EXPANDER_ADDR_LCD, 0x03, 0xFE);
 i2c_write_byte(I2C_EXPANDER_ADDR_LCD, 0x01, 0x01);

 lcd.begin(8, 2);
 lcd.setContrast(30);
 lcd.clear();
 lcd.print("NOW");
 lcd.setCursor(0, 1);
 lcd.print("BOOTING!");
#endif

  setupPort();
  delay(10);

  noInterrupts();
  //setupExtInt();
  setupTC2Int();
  interrupts();

  setupBLE();
#ifdef DEBUG
    Serial.println(F("setup end"));
#endif
  ble112.ble_cmd_system_get_bt_address();
  while (ble112.checkActivity(1000));
  delay(1000);

  MsTimer2::start();      // Timer2 inverval start
#ifdef DEBUG
    Serial.println(F(""));
    Serial.println(F("loop start"));
    Serial.println(F(""));
#endif
#if LCD
 lcd.clear();
 lcd.print("Waiting");
 lcd.setCursor(0, 1);
 lcd.print("connect");
#endif

  //pinMode(2, INPUT);
  attachInterrupt(0,catchHuman , FALLING );           //人接近検知割り込み
  //人感センサ設定
  i2c_write_byte(I2C_PIR_ADDR, 0x20, 0xFF); //CNTL1  Resrt 
  i2c_write_byte(I2C_PIR_ADDR, 0x2A, 0xF2); //CNTL11 人感アルゴリズム有効/割り込み出力有効
  i2c_write_byte(I2C_PIR_ADDR, 0x25, 0x0F); //CNTL6  センサゲイン205%(最大)
  i2c_write_byte(I2C_PIR_ADDR, 0x2B, 0xFF); //CNTL12 Mode=1 start Meas(連続測定モード)
  
}

//====================================================================
// loop
//====================================================================
void loop() {

  //-----------------------------------------------------
  // Timer2 interval　125ms で1回ループ
  //-----------------------------------------------------
  if (bInterval == true){
    Serial.println("*LOOP S*");
    bInterval = false;
    //--------------------------------------------
    // LED
    //--------------------------------------------
    if(bBLEsendData == true){
      iLed += 1;
      if(iLed >= iToggle){
        iLed = 0;
        digitalWrite(LED_PIN, bToggle);
        bToggle = !bToggle;
      }
    } else{
      digitalWrite(LED_PIN, LOW);
      iLed = 0;
    }
    //--------------------------------------------
    //--------------------------------------------
    // loop counter
    //--------------------------------------------
    loopCounter();
    //--------------------------------------------
    // sensor read
    //--------------------------------------------
    clearI2CReadbuf();
    i2c_read(I2C_PIR_ADDR, 0x04, 6, i2c_receiveBuf);
    sprintf(buf, "REG = %02X , %02X , %02X , %02X , %02X , %02X", i2c_receiveBuf[0], i2c_receiveBuf[1], i2c_receiveBuf[2], i2c_receiveBuf[3], i2c_receiveBuf[4], i2c_receiveBuf[5]);
    Serial.println(buf);
    sprintf(buf, "Human detection = %d", (i2c_receiveBuf[0] & 0x10) >> 4  );
    Serial.println(buf);

    //IRセンサ測定データ
    irData = clacIR();
    Serial.print("IR   = ");
    Serial.print(irData,2);
    Serial.println(" pA");
    datadist = irData ;
  
    
    //--------------------------------------------
    // BLE
    //--------------------------------------------
    loopBleSnd();

    //--------------------------------------------
    // sleep/resume
    //--------------------------------------------
    Serial.println("*LOOP E*");
 //   loopSleep();
  }
  loopBleRcv();
  if(bLCDchange == true)
  {
    
  }

}

// ================================================================
// INTERNAL BGLIB CLASS CALLBACK FUNCTIONS
// ================================================================

// called when the module begins sending a command
void onBusy() {
    // turn LED on when we're busy
    //digitalWrite( LED_PIN, HIGH );
}

// called when the module receives a complete response or "system_boot" event
void onIdle() {
    // turn LED off when we're no longer busy
    //digitalWrite( LED_PIN, LOW );
}

// called when the parser does not read the expected response in the specified time limit
void onTimeout() {
    // reset module (might be a bit drastic for a timeout condition though)
    //digitalWrite( BLE_RESET_PIN, LOW );
    //delay(5);                           // wait 5ms
    //digitalWrite( BLE_RESET_PIN, HIGH );
    // set state to ADVERTISING
    ble_state = BLE_STATE_ADVERTISING;

    // clear "encrypted" and "bonding" info
    ble_encrypted = 0;
    ble_bonding = 0xFF;
    /*  */
    bBLEconnect = false;                    /* [BLE] 接続状態 */
    bBLEsendData = false;
}

// called immediately before beginning UART TX of a command
void onBeforeTXCommand() {
}

// called immediately after finishing UART TX
void onTXCommandComplete() {
    // allow module to return to sleep (assuming here that digital pin 5 is connected to the BLE wake-up pin)
}
/*  */


void my_evt_gatt_server_attribute_value( const struct ble_msg_gatt_server_attribute_value_evt_t *msg ) {
    uint16 attribute = (uint16)msg -> attribute;
    uint16 offset = 0;
    uint8 value_len = msg -> value.len;

    uint8 value_data[20];
    String rcv_data;
    rcv_data = "";
    for (uint8_t i = 0; i < value_len; i++) {
        rcv_data += (char)(msg -> value.data[i]);
    }

#ifdef DEBUG
        Serial.print(F("###\tgatt_server_attribute_value: { "));
        Serial.print(F("connection: ")); Serial.print(msg -> connection, HEX);
        Serial.print(F(", attribute: ")); Serial.print((uint16_t)msg -> attribute, HEX);
        Serial.print(F(", att_opcode: ")); Serial.print(msg -> att_opcode, HEX);
#if 1
        Serial.print(", offset: "); Serial.print((uint16_t)msg -> offset, HEX);
        Serial.print(", value_len: "); Serial.print(msg -> value.len, HEX);
        Serial.print(", value_data: "); Serial.print(rcv_data);
#endif
        Serial.println(F(" }"));
#endif

    if( rcv_data.indexOf("SND") == 0 ){
        bBLEsendData = true;
        iToggle = 8;
    } else if( rcv_data.indexOf("STP") == 0 ){
        bBLEsendData = false;
        bLCDchange = true;
        lcd_view_sts = 1;
    } else if(rcv_data.indexOf("PLS") == 0){
      if(iToggle < 16){
        iToggle += 2;
      }
    } else if(rcv_data.indexOf("MNS") == 0){
      if(iToggle > 2){
        iToggle -= 2;
      }
    }
}
/*  */


void my_evt_le_connection_opend( const ble_msg_le_connection_opend_evt_t *msg ) {
    #ifdef DEBUG
        Serial.print(F("###\tconnection_opend: { "));
        Serial.print(F("address: "));
        // this is a "bd_addr" data type, which is a 6-byte uint8_t array
        for (uint8_t i = 0; i < 6; i++) {
            if (msg -> address.addr[i] < 16) Serial.write('0');
            Serial.print(msg -> address.addr[i], HEX);
        }
    #if 0
        Serial.print(", address_type: "); Serial.print(msg -> address_type, HEX);
        Serial.print(", master: "); Serial.print(msg -> master, HEX);
        Serial.print(", connection: "); Serial.print(msg -> connection, HEX);
        Serial.print(", bonding: "); Serial.print(msg -> bonding, HEX);
        Serial.print(", advertiser: "); Serial.print(msg -> advertiser, HEX);
    #endif
        Serial.println(" }");
    #endif

    /*  */
    ble_state = BLE_STATE_CONNECTED_SLAVE;
    bSleep = false;
    bLCDchange = true;
    lcd_view_sts = 1;
}
/*  */
void my_evt_le_connection_closed( const struct ble_msg_le_connection_closed_evt_t *msg ) {
    #ifdef DEBUG
        Serial.print(F("###\tconnection_closed: { "));
        Serial.print(F("reason: ")); Serial.print((uint16_t)msg -> reason, HEX);
        Serial.print(F(", connection: ")); Serial.print(msg -> connection, HEX);
        Serial.println(F(" }"));
    #endif

    // after disconnection, resume advertising as discoverable/connectable (with user-defined advertisement data)
    //ble112.ble_cmd_le_gap_set_mode( LE_GAP_GENERAL_DISCOVERABLE, LE_GAP_UNDIRECTED_CONNECTABLE );
    //ble112.ble_cmd_le_gap_start_advertising(1, LE_GAP_GENERAL_DISCOVERABLE, LE_GAP_UNDIRECTED_CONNECTABLE);
    ble112.ble_cmd_le_gap_start_advertising( 0, LE_GAP_USER_DATA, LE_GAP_UNDIRECTED_CONNECTABLE );    // index = 0
    while (ble112.checkActivity(1000));

    // set state to ADVERTISING
    ble_state = BLE_STATE_ADVERTISING;

    // clear "encrypted" and "bonding" info
    ble_encrypted = 0;
    ble_bonding = 0xFF;
    /*  */
    bBLEconnect = false;                    /* [BLE] 接続状態 */
    bBLEsendData = false;
    bSleep = false;
    bLCDchange = true;
    lcd_view_sts = 0;
}
/*  */

void my_evt_system_boot( const ble_msg_system_boot_evt_t *msg ) {
 #if 0
    #ifdef DEBUG
        Serial.print( "###\tsystem_boot: { " );
        Serial.print( "major: " ); Serial.print(msg -> major, HEX);
        Serial.print( ", minor: " ); Serial.print(msg -> minor, HEX);
        Serial.print( ", patch: " ); Serial.print(msg -> patch, HEX);
        Serial.print( ", build: " ); Serial.print(msg -> build, HEX);
    //    SerialUSB.print(", ll_version: "); Serial.print(msg -> ll_version, HEX);
        Serial.print( ", bootloader_version: " ); Serial.print( msg -> bootloader, HEX );           /*  */
    //    Serial.print(", protocol_version: "); Serial.print(msg -> protocol_version, HEX);
        Serial.print( ", hw: " ); Serial.print( msg -> hw, HEX );
        Serial.println( " }" );
    #endif
#endif

    // set state to ADVERTISING
    ble_state = BLE_STATE_ADVERTISING;
}
void my_evt_system_awake( const ble_msg_system_boot_evt_t *msg ) {

   ble112.ble_cmd_system_halt( 0 );
   while (ble112.checkActivity(1000));

  bBLESleep = false;

}
void my_rsp_system_get_bt_address(const struct ble_msg_system_get_bt_address_rsp_t *msg ){
#ifdef DEBUG
  Serial.print( "###\tsystem_get_bt_address: { " );
  Serial.print( "address: " );
  for (int i = 0; i < 6 ;i++){
    Serial.print(msg->address.addr[i],HEX);
  }
  Serial.println( " }" );
#endif
  unsigned short addr = 0;
  char cAddr[30];
  addr = msg->address.addr[0] + (msg->address.addr[1] *0x100);
  sprintf(cAddr, "Device name is Leaf_A_#%05d ",addr);
  Serial.println(cAddr);
}
