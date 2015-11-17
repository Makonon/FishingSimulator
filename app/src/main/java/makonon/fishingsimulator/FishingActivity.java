package makonon.fishingsimulator;

import java.util.List;

import android.app.Activity;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.widget.TextView;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import java.nio.*;
import android.widget.*;

public class FishingActivity extends Activity{

    double dbltmp = 0;
    int inttmp = 0;
    String strtmp = "";

	/* クラス　*/
	FishingSensorAdapter esa;
	FishingSimThread est;
    FishingActionThread eat;

    ToggleButton tglLever;

	/* シミュレーションパラメータ */
    int intSimStatus = 0; //ステータス [0:スタンバイ 1:キャスト 2:エギング 3:ファイト 4:キャッチ]
    double dblSimGyroV = 0;
    double dblSimCastPowerMax = 0;
	double dblSimRodAngle = 0; //[rad]
    double dblSimShakePower = 0;
    double dblSimLineLength = 0; //ライン長[mm]
    double dblSimSlackLength = 0; //スラック長[mm]
    double dblSimLineTension = 0; //ラインテンション[m/s]
    double dblSimRodX = 0;
    double dblSimRodY = 0;
    double dblSimLureVx = 0;
    double dblSimLureVy = 0;
    double dblSimLureX = 0;
    double dblSimLureY = 0;
    boolean boolCastPower = false;
    double dblSimLureArad = 0;
    double dblSimLureVrad = 0;
    double dblSimLureAngle = 0;
    double dblSimLureA = 0;
    double dblSimLureAx = 0;
    double dblSimLureAy = 0;    
    double dblSimReelAngle = 0;
    double dblSimReelRollSpeed = 1.0;
    int intSimReelRollCoeff = 0;
    double dblSimReelV = 0;
    double dblSimReelVx = 0;
    double dblSimReelVy = 0;
    double dblSimFootHeight = 1.5;
	boolean boolSimReelLever = false;
    boolean boolSimLineTouch = false;
    boolean boolSimShake = false;
    boolean boolSimStatusChange = false;
    double dblSimWind = 0.1;
    int intEncount = 0;
    int intEncountField = 20;
    int intEncountBonus = 0;
    boolean boolHooking = false;
    boolean boolAttack = false;
    int intAttackCount = 0;
    double[] dblSimBottomDepth = new double[100]; //m

	/* プレイヤーパラメータ */
	int intLineStrength; //ライン強度[lb]
	int intReelDragMax; //ドラグ最大値[g]
	double dblReelRollLength = 0.7; //巻上長[mm]
    double dblRodLength = 8.6 * 0.3048;
    double dblRodBending = 1.2;
	double dblRodGuideFriction = -1.0;

	/* エギパラメータ */
	double dblLureSize = 3.5; //号数
    double dblLureWeight = 20.0; //"
	int intLureBaseColor; //ベース（下地）色
	int intLureBodyColor; //ボディ（布地）色
	double dblLureFallSpeed = -3.2; //フォールスピード[s/m]
	double dbllLureBalance = 1.0; //バランス性能

	/* Jig Head */
	double dblJHDencity = 11340; //kg/m3
	double dblJHWeight = 0.0012; //kg
	double dblJHType = 0; //0:Round 1:Rocket 2:Dart
	double dblJHArea = 0;
	double dblJHVolume = 0;

	/* Warm */
	double dblWarmDencity = 1255; //kg/m3
    double dblWarmWeight = 0;
	double dblWarmLength = 0.0381; //m -> 1.5inch
	double dblWarmRadius = 0.0030;//m
	double dblWarmArea = 0;
	double dblWarmVolume = 0;

	/* fishパラメータ */
    boolean boolFishingActionThread = true;
    boolean boolFishingViewThread = true;
	double inch2m = 0.0254;
    double dt = 0.05;
    double g = -9.80665;
    double AirDencity = 1.2250; //kg/m3
    double SeaDencity = 1025; //kg/m3
    double Cd = 0.9; //0.44;
    double pi = 3.14159;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fishing);

		// センサ・マネージャー
		SensorManager sensmanager = (SensorManager) getSystemService(SENSOR_SERVICE);

		// センサーアダプタを生成
        esa = new FishingSensorAdapter(sensmanager);

        // スレッド生成および開始
        est = new FishingSimThread();
        eat = new FishingActionThread(esa);
        est.start();
        eat.start();

		dblJHVolume = dblJHWeight / dblJHDencity;
        dblJHArea = pi * Math.pow((Math.pow(dblJHVolume * 0.75 / pi,0.33333333)),2.0);
		dblWarmVolume = pi * Math.pow(dblWarmRadius,2.0) * dblWarmLength;
		dblWarmArea = dblWarmLength * (dblWarmRadius * 2.0);
		dblWarmWeight = dblWarmVolume * dblWarmDencity;

        for(inttmp = 0; inttmp < 100; inttmp++){
            dblSimBottomDepth[inttmp] = -Math.sqrt(inttmp) - 3;
        }

        findViewById(R.id.btnLine).setOnTouchListener(btnLineTouchListener);
        findViewById(R.id.btnReel).setOnTouchListener(btnReelTouchListener);
        findViewById(R.id.btnShake).setOnTouchListener(btnShakeTouchListener);

        tglLever = (ToggleButton)findViewById(R.id.tglLever);
        tglLever.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
				// トグルボタンがクリックされたと時のハンドラ
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					// TODO Auto-generated method stub
					// トグルボタンの状態が変更された時の処理を記述
					/*if(isChecked) {
						boolSimReelLever = true;
					}else {
						boolSimReelLever = false;
					}*/
                    boolSimReelLever = isChecked;
				}
			});

        SeekBar barReelSpeed = (SeekBar)findViewById(R.id.barReelSpeed);
        barReelSpeed.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            // トラッキング開始時に呼び出されます
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            // トラッキング中に呼び出されます
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
                dblSimReelRollSpeed = (double) (progress + 1) / 10;
            }

            // トラッキング終了時に呼び出されます
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                strtmp = String.valueOf(dblSimReelRollSpeed) + "回転／秒";
                Toast.makeText(getApplicationContext(), strtmp, Toast.LENGTH_SHORT).show();
            }
        });
    }

	@Override
	protected void onPause() {
	    super.onPause();
	    // アプリが終了するためセンサー類の停止
	    esa.stopSensor();
        // アプリが終了するためスレッドを終了
        eat.close();
        est.close();
	}

    double getAirForce(double v){
		dbltmp = dblJHArea + dblWarmArea;
		dbltmp = Cd * AirDencity * dbltmp * (Math.pow(v,2)) / 2;
        if(v>0){dbltmp = -dbltmp;}
		return(dbltmp);
    }

	double getSeaForce(double v){
        dbltmp = dblJHArea + dblWarmArea;
		dbltmp = Cd * SeaDencity * dbltmp * (Math.pow(v,2)) / 2;
        if(v>0){dbltmp = -dbltmp;}
		return(dbltmp);
    }

    private OnTouchListener btnLineTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:    //タッチする
                    switch(intSimStatus){
                        case 0: //スタンバイ状態の時
                            boolCastPower = true;
                            tglLever.setChecked(false);
                            break;
                        case 1:
                            boolSimLineTouch = true;
                            break;
                        case 2:
                            boolSimLineTouch = true;
                            break;
                    }
                    break;
                case MotionEvent.ACTION_MOVE:    //タッチしたまま動かす
                    break;
                case MotionEvent.ACTION_UP:        //指を離す
                    switch(intSimStatus){
                        case 0: //スタンバイ状態の時
                            if((dblSimRodAngle > 10) && (dblSimRodAngle < 90) && (dblSimCastPowerMax > 0)){
                                intSimStatus = 1;
                                dblSimLineLength = dblRodLength / 2;
                                dblSimSlackLength = dblSimLineLength;
                                dbltmp = Math.toRadians(dblSimRodAngle * 3 / 4);
                                dblSimLureVx = dblSimCastPowerMax * 0.5 * Math.cos(dbltmp);
                                dblSimLureVy = dblSimCastPowerMax * 0.5 * Math.sin(dbltmp);
                                dblSimLureX = dblSimRodX + dblSimLureVx * dt;
                                dblSimLureY = dblSimRodY + dblSimLureVy * dt;
                                strtmp = "Cast \n" + String.valueOf(Math.round(Math.toDegrees(dbltmp))) + "\n" + String.valueOf(dblSimCastPowerMax);
                                Toast.makeText(getApplicationContext(),strtmp,Toast.LENGTH_LONG).show();
                            }else{
                                strtmp = "Cast Miss";
                                Toast.makeText(getApplicationContext(),strtmp,Toast.LENGTH_LONG).show();
                            }
                            break;
                        case 1:
                            boolSimLineTouch = false;
                            break;
                        case 2:
                            boolSimLineTouch = false;
                            break;
                    }
                    boolCastPower = false;
                    break;
            }
            return true;
        }
    };

    private OnTouchListener btnReelTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:    //タッチする
                    intSimReelRollCoeff = 1;
                    break;
                case MotionEvent.ACTION_MOVE:    //タッチしたまま動かす
                    break;
                case MotionEvent.ACTION_UP:        //指を離す
                    intSimReelRollCoeff = 0;
                    break;
            }
            return true;
        }
    };

    private OnTouchListener btnShakeTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:    //タッチする
                    //boolFishingActionThread = false;
                    //boolFishingViewThread = false;
                    break;
                case MotionEvent.ACTION_MOVE:    //タッチしたまま動かす
                    break;
                case MotionEvent.ACTION_UP:        //指を離す
                    //boolFishingActionThread = true;
                    //boolFishingViewThread = true;
                    strtmp = String.valueOf(dblJHVolume) + "\n" + String.valueOf(dblJHArea) + "\n" + String.valueOf(dblWarmVolume) + "\n" + String.valueOf(dblWarmArea) + "\n" + String.valueOf(dblWarmWeight);
                    Toast.makeText(getApplicationContext(),strtmp,Toast.LENGTH_LONG).show();

                    break;
            }
            return true;
        }
    };

    class FishingActionThread extends Thread{
        /* クラス　*/
        FishingSensorAdapter esa;

        Handler handler = new Handler();

        int intruncnt = 0;

        Vibrator vib = (Vibrator)getSystemService(VIBRATOR_SERVICE);

        public FishingActionThread(FishingSensorAdapter esa) {
            this.esa = esa;
        }

        public void close() {
            // 描画スレッドの停止
            boolFishingActionThread = false;
        }

        public void UpdateSimGyroV(boolean boolEnable){
            if(!boolEnable){
                dblSimGyroV = 0;
            }else if(intSimStatus == 0){
                dblSimGyroV = -esa.getGyroX() * dblRodLength * dblRodBending;
            }else{
                dblSimGyroV = esa.getGyroX() * dblRodLength * (1 / dblRodBending);
            }

            if(dblSimGyroV < 3){dblSimGyroV = 0;}
        }

        public void UpdateSimRodPos(boolean boolEnable){
            if(!boolEnable){
                dblSimRodAngle = 0;
                dblSimRodX = 0;
                dblSimRodY = 0;
                dblSimLureAngle = 0;
            }else{
                dblSimRodAngle = -Math.round(esa.getAnglePit());
                dbltmp = Math.toRadians(dblSimRodAngle);
                dblSimRodX = dblRodLength * Math.cos(dbltmp);
                dblSimRodY = dblRodLength * Math.sin(dbltmp) + dblSimFootHeight;
                dblSimLureAngle = Math.atan((dblSimLureY - dblSimRodY) / (dblSimLureX - dblSimRodX));
            } 
        }

        public void UpdateSimReelV(boolean boolEnable){
            if(!boolEnable){
                dblSimReelAngle = 0;
                dblSimReelV = 0;
                dblSimReelVx =  0;
                dblSimReelVy =  0;
            }else{
                dblSimReelAngle = dblSimLureAngle;
                dblSimReelV = intSimReelRollCoeff * (dblReelRollLength * dblSimReelRollSpeed);
                dbltmp = Math.toRadians(dblSimRodAngle);
                dblSimReelVx =  -dblSimReelV * Math.cos(dbltmp);                    
                dblSimReelVy =  dblSimReelV * Math.sin(dbltmp);
            }            
        }

        public void UpdateLength(boolean boolEnable){
            if(!boolEnable){
                dblSimLineLength = 0;
                dblSimSlackLength = 0;
            }else if(boolSimReelLever){
                dblSimLineLength = dblSimLineLength - (dblSimReelV * dt);
                dblSimSlackLength = dblSimLineLength - Math.sqrt(Math.pow(dblSimLureX - dblSimRodX,2) + Math.pow(dblSimLureY - dblSimRodY,2)) - (dblSimReelV * dt);
            }else if(boolSimLineTouch){
                dblSimSlackLength = dblSimLineLength - Math.sqrt(Math.pow(dblSimLureX - dblSimRodX,2) + Math.pow(dblSimLureY - dblSimRodY,2));
            }else if((dblSimLureVy > 0) && (intSimStatus == 1)){
                dblSimLineLength = dblSimLineLength + Math.abs(dblSimLureVx * dt);
                dblSimSlackLength = dblSimLineLength - Math.sqrt(Math.pow(dblSimLureX - dblSimRodX,2) + Math.pow(dblSimLureY - dblSimRodY,2));
            }else{
                dblSimLineLength = dblSimLineLength + Math.sqrt(Math.pow(dblSimLureVx * dt,2) + Math.pow(dblSimLureVy * dt,2));
                dblSimSlackLength = dblSimLineLength - Math.sqrt(Math.pow(dblSimLureX - dblSimRodX,2) + Math.pow(dblSimLureY - dblSimRodY,2));
            }

            if(dblSimLineLength < 0){dblSimLineLength = 0;}
            if(dblSimSlackLength < 0){dblSimSlackLength = 0;}
        }

        public void UpdateSimLureV(boolean boolEnable){
            if(!boolEnable){
                dblSimLureVx = 0;
                dblSimLureVy = 0;
            }else if(dblSimLureY > 0){
				if(dblSimLureAx < 0){
					dbltmp = 0;
				}else{
					dbltmp = dblRodGuideFriction;
				}
                dblSimLureVx = dblSimLureVx + ((dblSimLureAx + dbltmp) * dt);

				if(dblSimLureAy < 0){
					dbltmp = 0;
				}else{
					dbltmp = dblRodGuideFriction;
				}
                dblSimLureVy = dblSimLureVy + ((dblSimLureAy + dbltmp) * dt);
            }else if(dblSimLureY == dblSimBottomDepth[(int)Math.round(dblSimLureX)]){
                dblSimLureVx = 0;
                dblSimLureVy = 0;
            }else{
                dblSimLureVx = dblSimLureVx + (dblSimLureAx * dt);
                dblSimLureVy = dblSimLureVy + (dblSimLureAy * dt);

                //if(dblSimLureVy < (1 / dblLureFallSpeed)){dblSimLureVy = (1 / dblLureFallSpeed);}
            }            
        }

        public void UpdateSimLureVrad(boolean boolEnable){
            if(!boolEnable || (dblSimSlackLength > 0)){
                dblSimLureVrad = 0;/*
				 }else if(dblSimLureY > 0){
				 dblSimLureVrad = dblSimLureVrad + dblSimLureArad * dt;*/
            }else{
                //dblSimLureVrad = (1 / dblLureFallSpeed) * Math.cos(dblSimLureAngle) / dblSimLineLength;
                dblSimLureVrad = dblSimLureVrad + dblSimLureArad * dt;
            }            
        }

        public void UpdateSimLurePos(boolean boolEnable){
            if(!boolEnable){
                dblSimLureX = dblSimRodX;
                dblSimLureY = dblSimRodY;
            }else if((dblSimSlackLength > 0) || (!boolSimReelLever && !boolSimLineTouch)){
                dblSimLureX = dblSimLureX + dblSimLureVx * dt;
                dblSimLureY = dblSimLureY + dblSimLureVy * dt;
            }else{
                dbltmp = dblSimLureAngle + (dblSimLureVrad * dt);
                dblSimLureX = dblSimLineLength * Math.cos(dbltmp) + dblSimRodX + (dblSimLureVx * dt);
                dblSimLureY = dblSimLineLength * Math.sin(dbltmp) + dblSimRodY + (dblSimLureVy * dt);
            }

            if(dblSimLureX <= dblSimRodX){dblSimLureX = dblSimRodX;}
            if(intSimStatus == 2){
                if(dblSimLureY > 0){dblSimLureY = 0;}
                if(dblSimLureY < dblSimBottomDepth[(int)Math.round(dblSimLureX)]){dblSimLureY = dblSimBottomDepth[(int)Math.round(dblSimLureX)];}
            }
        }

        public void UpdateEncount(boolean boolEnable){
            if(!boolEnable){
                intEncount = 0;
                intEncountBonus = 0;
                intAttackCount = 0;
                boolAttack = false;
                boolHooking = false;
            }else{
                intEncountBonus = intEncountBonus + (int)(Math.abs(1 * dblSimLureAx) + Math.abs(1 * dblSimLureAy));
                intEncountBonus /= 2;
                intEncount = intEncountField + intEncountBonus;
                if(intEncount < 0){intEncount = 0;}

                if(boolAttack){
                    intAttackCount -= dt;
                    if(intAttackCount <= 0){
                        intAttackCount = 0;
                        boolAttack = false;
                    }else{
                        if(dblSimSlackLength <= 0){
                            dbltmp = Math.sqrt(Math.pow(dblSimLureVx,2) + Math.pow(dblSimLureVy,2)) * 0.01;
                        }else{
                            dbltmp = 0;
                        }
                        dbltmp = dbltmp + (Math.random() * (1 + dbltmp));
                        if(dbltmp > 0.99){
                            boolAttack = false;
                            boolHooking = true;
                        }
                    }
                }else{
                    inttmp = (int)(Math.random() * 10000);
                    if(inttmp < intEncount){
                        intAttackCount = (int)(Math.random() * (1.5 * (1 / dt))); //max 1.5sec
                        boolAttack = true;
                        inttmp = (int)(((Math.random() * 0.5) - dblSimSlackLength) * 1000); // max 0.5sec
                        vib.vibrate(inttmp);
                    }
                }
            }
        }

        public void run() {
            while (boolFishingActionThread) {
                // ハンドルクラスによるUI描画
                handler.post(new Runnable() {
						@Override
						public void run() {
							switch(intSimStatus){
								case 0:
									UpdateSimGyroV(true);
									UpdateSimRodPos(true);
									UpdateSimReelV(false);
									UpdateLength(false);                                
									dblSimLureAx = 0;
									dblSimLureAy = 0;
									dblSimLureArad = 0;
									UpdateSimLureV(false);
									UpdateSimLureVrad(false);
									UpdateSimLurePos(false);
									UpdateEncount(false);

									if(boolCastPower = false){
										dblSimCastPowerMax = 0;
									}else{
										if(dblSimGyroV > dblSimCastPowerMax){
											dblSimCastPowerMax = dblSimGyroV;
										}
									}
									break;
								case 1:
									if(boolSimReelLever || boolSimLineTouch){
										if(dblSimSlackLength <= 0){
											UpdateSimGyroV(false);
											UpdateSimRodPos(true);
											UpdateSimReelV(false);
											UpdateLength(true);
											dblSimLureAx = 0;
											dblSimLureAy = (getAirForce(dblSimLureVy) / (dblJHWeight + dblWarmWeight)) + g;
											dblSimLureArad = dblSimLureAy * Math.cos(dblSimLureAngle) / dblSimLineLength;
											UpdateSimLureV(false);
											UpdateSimLureVrad(true);
											UpdateSimLurePos(true);
											UpdateEncount(false);

											if(dblSimGyroV > 0){vib.vibrate(50);}
										}else{
											UpdateSimGyroV(false);
											UpdateSimRodPos(true);
											UpdateSimReelV(false);
											UpdateLength(true);
											dblSimLureAx = (getAirForce(dblSimLureVx) / (dblJHWeight + dblWarmWeight));
											dblSimLureAy = (getAirForce(dblSimLureVy) / (dblJHWeight + dblWarmWeight)) + g;
											dblSimLureArad = 0;
											UpdateSimLureV(true);
											UpdateSimLureVrad(false);
											UpdateSimLurePos(true);
											UpdateEncount(false);
										}
									}else{
										UpdateSimGyroV(false);
										UpdateSimRodPos(true);
										UpdateSimReelV(false);
										UpdateLength(true);
										dblSimLureAx = (getAirForce(dblSimLureVx) / (dblJHWeight + dblWarmWeight));
										dblSimLureAy = (getAirForce(dblSimLureVy) / (dblJHWeight + dblWarmWeight)) + g;
										dblSimLureArad = 0;
										UpdateSimLureV(true);
										UpdateSimLureVrad(false);
										UpdateSimLurePos(true);
										UpdateEncount(false);
                                    }

									if(dblSimLineLength < (dblRodLength / 2)){
										UpdateSimGyroV(false);
										UpdateSimRodPos(true);
										UpdateSimReelV(false);
										UpdateLength(false);                                
										dblSimLureAx = 0;
										dblSimLureAy = 0;
										dblSimLureArad = 0;
										UpdateSimLureV(false);
										UpdateSimLureVrad(false);
										UpdateSimLurePos(false);


										intSimStatus = 0;
									}
									if(dblSimLureY <= 0){
										UpdateSimGyroV(false);
										UpdateSimRodPos(true);
										UpdateSimReelV(false);
										UpdateLength(true);
										dblSimLureAx = 0;
										dblSimLureAy = 0;
										dblSimLureArad = 0;
										UpdateSimLureV(false);
										UpdateSimLureVrad(false);
										UpdateSimLurePos(true);
										UpdateEncount(false);

										intSimStatus = 2;
									}
									break;
								case 2:
									if(boolSimReelLever){
										if(dblSimSlackLength <= 0){
											UpdateSimGyroV(true);
											UpdateSimRodPos(true);
											UpdateSimReelV(true);
											UpdateLength(true);
											dbltmp = getSeaForce(dblSimLureVx);
											dblSimLureAx = (dbltmp / (dblJHWeight + dblWarmWeight)) + (Math.sqrt(dblSimGyroV) * Math.cos(-dblSimLureAngle));
											dbltmp = (((dblJHDencity - SeaDencity) * dblJHVolume * g) + ((dblWarmDencity - SeaDencity) * dblWarmVolume * g) / 2) + getSeaForce(dblSimLureVy);
											dblSimLureAy = (dbltmp / (dblJHWeight + dblWarmWeight)) + (Math.sqrt(dblSimGyroV) * Math.sin(-dblSimLureAngle));
											dblSimLureArad = dblSimLureAy * Math.cos(dblSimLureAngle) / dblSimLineLength;
											UpdateSimLureV(true);
											UpdateSimLureVrad(true);
											UpdateSimLurePos(true);
											UpdateEncount(true);

											if(dblSimGyroV != 0){vib.vibrate(50);}
										}else{
											UpdateSimGyroV(true);
											UpdateSimRodPos(true);
											UpdateSimReelV(true);
											UpdateLength(true);
											dbltmp = getSeaForce(dblSimLureVx);
											dblSimLureAx = (dbltmp / (dblJHWeight + dblWarmWeight));
											dbltmp = (((dblJHDencity - SeaDencity) * dblJHVolume * g) + ((dblWarmDencity - SeaDencity) * dblWarmVolume * g) / 2) + getSeaForce(dblSimLureVy);
											dblSimLureAy = (dbltmp / (dblJHWeight + dblWarmWeight));
											dblSimLureArad = 0;
											UpdateSimLureV(true);
											UpdateSimLureVrad(false);
											UpdateSimLurePos(true);
											UpdateEncount(true);
										}
									}else if(boolSimLineTouch){
										if(dblSimSlackLength <= 0){
											UpdateSimGyroV(true);
											UpdateSimRodPos(true);
											UpdateSimReelV(false);
											UpdateLength(true);
											dbltmp = getSeaForce(dblSimLureVx);
											dblSimLureAx = (dbltmp / (dblJHWeight + dblWarmWeight)) + (Math.sqrt(dblSimGyroV) * Math.cos(-dblSimLureAngle));
											dbltmp = (((dblJHDencity - SeaDencity) * dblJHVolume * g) + ((dblWarmDencity - SeaDencity) * dblWarmVolume * g) / 2) + getSeaForce(dblSimLureVy);
											dblSimLureAy = (dbltmp / (dblJHWeight + dblWarmWeight)) + (Math.sqrt(dblSimGyroV) * Math.sin(-dblSimLureAngle));
											dblSimLureArad = dblSimLureAy * Math.cos(dblSimLureAngle) / dblSimLineLength;
											UpdateSimLureV(true);
											UpdateSimLureVrad(true);
											UpdateSimLurePos(true);
											UpdateEncount(true);

											if(dblSimGyroV > 0){vib.vibrate(50);}
										}else{
											UpdateSimGyroV(true);
											UpdateSimRodPos(true);
											UpdateSimReelV(false);
											UpdateLength(true);
											dbltmp = getSeaForce(dblSimLureVx);
											dblSimLureAx = (dbltmp / (dblJHWeight + dblWarmWeight));
											dbltmp = (((dblJHDencity - SeaDencity) * dblJHVolume * g) + ((dblWarmDencity - SeaDencity) * dblWarmVolume * g) / 2) + getSeaForce(dblSimLureVy);
											dblSimLureAy = (dbltmp / (dblJHWeight + dblWarmWeight));
											dblSimLureArad = 0;
											UpdateSimLureV(true);
											UpdateSimLureVrad(false);
											UpdateSimLurePos(true);
											UpdateEncount(true);
										}
									}else{
										UpdateSimGyroV(true);
										UpdateSimRodPos(true);
										UpdateSimReelV(false);
										UpdateLength(true);
										dbltmp = getSeaForce(dblSimLureVx);
										dblSimLureAx = (dbltmp / (dblJHWeight + dblWarmWeight));
										dbltmp = (((dblJHDencity - SeaDencity) * dblJHVolume * g) + ((dblWarmDencity - SeaDencity) * dblWarmVolume * g) / 2) + getSeaForce(dblSimLureVy);
										dblSimLureAy = (dbltmp / (dblJHWeight + dblWarmWeight));
										dblSimLureArad = 0;
										UpdateSimLureV(true);
										UpdateSimLureVrad(false);
										UpdateSimLurePos(true);
										UpdateEncount(true);
									}

									if(dblSimLineLength < (dblRodLength / 2)){
										intSimStatus = 0;
									}

									if(boolHooking){
										intSimStatus = 0;
										strtmp = "Hooking!!!";
										Toast.makeText(getApplicationContext(),strtmp,Toast.LENGTH_LONG).show();
										vib.vibrate(1000);
									}
									break;
							}
						}
					});
                try {
                    // 100ミリ秒毎の更新
                    Thread.sleep((int)(dt*1000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }        
	}

	class FishingSimThread extends Thread{
		/* クラス　*/		
	    Handler handler = new Handler();

        TextView tvSimStatus;
        TextView tvSimCastPower;
        TextView tvSimRodAngle;
        TextView tvSimGyroV;
        TextView tvSimLineLength;
        TextView tvSimSlackLength;
        TextView tvSimLineTension;
        TextView tvSimLureVx;
        TextView tvSimLureVy;
        TextView tvSimLureX;
        TextView tvSimLureY;
        TextView tvSimReelV;
        TextView tvSimLureAngle;
        TextView tvSimLureAx;
        TextView tvSimLureAy;
        TextView tvEncount;
        TextView tvAttackCount;

		ProgressBar pgbSlack;

        FishingGraphView grpos;

	    public FishingSimThread() {
            // テキストビュー
            tvSimStatus = (TextView)findViewById(R.id.tvSimStatus); //ステータス [0:スタンバイ 1:キャスト 2:エギング 3:ファイト 4:キャッチ]
            tvSimCastPower = (TextView)findViewById(R.id.tvSimCastPower);
            tvSimRodAngle = (TextView)findViewById(R.id.tvSimRodAngle); //飛距離[mm]
            tvSimGyroV = (TextView)findViewById(R.id.tvSimGyroV);
            tvSimLineLength = (TextView)findViewById(R.id.tvSimLineLength); //ライン長[mm]
            tvSimSlackLength = (TextView)findViewById(R.id.tvSimSlackLength); //スラック長[mm]
            tvSimLineTension = (TextView)findViewById(R.id.tvSimLineTension); //ラインテンション[g]
            tvSimLureVx = (TextView)findViewById(R.id.tvSimLureVx);
            tvSimLureVy = (TextView)findViewById(R.id.tvSimLureVy);
            tvSimLureX = (TextView)findViewById(R.id.tvSimLureX);
            tvSimLureY = (TextView)findViewById(R.id.tvSimLureY);
            tvSimReelV = (TextView)findViewById(R.id.tvSimReelV);
            tvSimLureAngle = (TextView)findViewById(R.id.tvSimLureAngle);
            tvSimLureAx = (TextView)findViewById(R.id.tvSimLureAx);
            tvSimLureAy = (TextView)findViewById(R.id.tvSimLureAy);
            tvEncount = (TextView)findViewById(R.id.tvEncount);
            tvAttackCount = (TextView)findViewById(R.id.tvAttackCount);

			pgbSlack = (ProgressBar) findViewById(R.id.pgbSlack);
			// 水平プログレスバーの最大値を設定します
			pgbSlack.setMax(300);
			// 水平プログレスバーの値を設定します
			pgbSlack.setProgress(0);
			// 水平プログレスバーのセカンダリ値を設定します
			pgbSlack.setSecondaryProgress(0);

            grpos = (FishingGraphView) findViewById(R.id.FishingGraphView);
	    }

	    public void close() {
	        // 描画スレッドの停止
	        boolFishingViewThread = false;
	    }

	    public void run() {
	        while (boolFishingViewThread) {
	            // ハンドルクラスによるUI描画
	            handler.post(new Runnable() {
						@Override
						public void run() {
							tvSimStatus.setText(String.valueOf(intSimStatus));
							tvSimCastPower.setText(String.valueOf((double)Math.round(dblSimCastPowerMax * 100) / 100));
							tvSimRodAngle.setText(String.valueOf(Math.round(dblSimRodAngle)));
							tvSimGyroV.setText(String.valueOf((double)Math.round(dblSimGyroV * 100) / 100));
							tvSimLineLength.setText(String.valueOf((double)Math.round(dblSimLineLength * 100) / 100));
							tvSimSlackLength.setText(String.valueOf((double)Math.round(dblSimSlackLength * 100) / 100));
							tvSimLureVx.setText(String.valueOf((double)Math.round(dblSimLureVx * 100) / 100));
							tvSimLureVy.setText(String.valueOf((double)Math.round(dblSimLureVy * 100) / 100));
							tvSimLureX.setText(String.valueOf((double)Math.round(dblSimLureX * 100) / 100));
							tvSimLureY.setText(String.valueOf((double)Math.round(dblSimLureY * 100) / 100));
							tvSimReelV.setText(String.valueOf((double)Math.round(dblSimLureVrad * 100) / 100));
							tvSimLureAngle.setText(String.valueOf((double)Math.round(Math.toDegrees(dblSimLureAngle) * 100) / 100));
							tvSimLureAx.setText(String.valueOf((double)Math.round(dblSimLureAx * 100) / 100));
							tvSimLureAy.setText(String.valueOf((double)Math.round(dblSimLureAy * 100) / 100));
							tvEncount.setText(String.valueOf(intEncount));
							tvAttackCount.setText(String.valueOf(intAttackCount));

							pgbSlack.setProgress((int)Math.round(300 - (dblSimSlackLength * 100)));

							switch(intSimStatus){
								case 0: //スタンバイ状態の時   
									grpos.initLurePos();
									grpos.setLurePos(dblSimLureX,dblSimLureY);
									for (inttmp = 0; inttmp < 100; inttmp++){
										grpos.setBtmPos(inttmp,dblSimBottomDepth[inttmp]);
									}
									break;
								case 1:
									grpos.setLurePos(dblSimLureX,dblSimLureY);                                
									grpos.invalidate();// グラフビュー再描画
									break;
								case 2:
									grpos.setLurePos(dblSimLureX,dblSimLureY);                                
									grpos.invalidate();// グラフビュー再描画
									break;
							}
						}
					});
	            try {
	                // 100ミリ秒毎の描画とします。
	                Thread.sleep((int)(dt*1000));
	            } catch (InterruptedException e) {
	                e.printStackTrace();
	            }
	        }
	    }        
	}
}

