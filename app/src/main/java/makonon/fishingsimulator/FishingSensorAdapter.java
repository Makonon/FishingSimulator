package makonon.fishingsimulator;

import java.util.List;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class FishingSensorAdapter implements SensorEventListener {
	/* センサーパラメータ */
	private SensorManager mSensorManager;
	private static final int MATRIX_SIZE = 16;
// 回転行列
	float[] floatInR = new float[MATRIX_SIZE];
	float[] floatOutR = new float[MATRIX_SIZE];
	float[] floatI = new float[MATRIX_SIZE];
// センサーの値
	float[] floatOrientationValues   = new float[3];
	float[] floatOrientationValuesLPF = new float[3];
	float[] floatMagneticValues      = new float[3];
	float[] floatAccelerometerValues = new float[3];
// ローパスフィルタ
	float fltOrientX = 0;
	float fltOrientY = 0;
	float fltOrientZ = 0;
// ハイパスフィルタ
	float fltAccelX = 0;
	float fltAccelY = 0;
	float fltAccelZ = 0;

	float fltGyroX = 0;
	float fltGyroY = 0;
	float fltGyroZ = 0;

	float fltAngleAzi = 0;
	float fltAnglePit = 0;
	float fltAngleRol = 0;
// 
	float fltAccelMaxX = 0;
	float fltAccelMaxY = 0;
	float fltAccelMaxZ = 0;
	float fltAccelMinX = 0;
	float fltAccelMinY = 0;
	float fltAccelMinZ = 0;

	boolean boolAccelMaxHold = true;
	boolean boolAccelMinHold = true;

	public float getAngleAzi(){
		return fltAngleAzi;
	}

	public float getAnglePit(){
		return fltAnglePit;
	}

	public float getAngleRol(){
		return fltAngleRol;
	}

	public float getAngleX(){
		return floatOrientationValuesLPF[0];
	}

	public float getAngleZ(){
		return floatOrientationValuesLPF[1];
	}

	public float getAngleY(){
		return floatOrientationValuesLPF[2];
	}

	public float getAccelX() {
		return fltAccelX;
	}

	public float getAccelY() {
		return fltAccelY;
	}

	public float getAccelZ() {
		return fltAccelZ;
	}

	public float getGyroX() {
		return fltGyroX;
	}

	public float getGyroY() {
		return fltGyroY;
	}

	public float getGyroZ() {
		return fltGyroZ;
	}

	public float getAccelMaxX() {
		return fltAccelMaxX;
	}

	public float getAccelMaxY() {
		return fltAccelMaxY;
	}

	public float getAccelMaxZ() {
		return fltAccelMaxZ;
	}

	public float getAccelMinX() {
		return fltAccelMinX;
	}

	public float getAccelMinY() {
		return fltAccelMinY;
	}

	public float getAccelMinZ() {
		return fltAccelMinZ;
	}

	public void setAccelMaxHold(boolean boolAccelMaxHold) {
		this.boolAccelMaxHold = boolAccelMaxHold;
		if(boolAccelMaxHold == true){
			fltAccelMaxX = 0;
			fltAccelMaxY = 0;
			fltAccelMaxZ = 0;
		}
	}

	public void setAccelMinHold(boolean boolAccelMinHold) {
		this.boolAccelMinHold = boolAccelMinHold;
		if(boolAccelMinHold == true){
			fltAccelMinX = 0;
			fltAccelMinY = 0;
			fltAccelMinZ = 0;
		}
	}

	public FishingSensorAdapter(SensorManager mSensorManager) {		
		// センサの取得
		List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
		// センサマネージャへリスナーを登録(implements SensorEventListenerにより、thisで登録する)
		for (Sensor sensor : sensors) {
			if( sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
				mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
			}
			if( sensor.getType() == Sensor.TYPE_ACCELEROMETER){
				mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
			}
			if( sensor.getType() == Sensor.TYPE_GYROSCOPE){
				mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
			}
			if( sensor.getType() == Sensor.TYPE_ORIENTATION){
				mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
			}
		}
	}

	public void stopSensor() {
		// センサー停止時のリスナ解除
		if ( mSensorManager != null ){
			mSensorManager.unregisterListener(this);
		}
		mSensorManager = null;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {}

	@Override
	public void onSensorChanged(SensorEvent event) {
		switch(event.sensor.getType()) {
			case Sensor.TYPE_ACCELEROMETER:
				floatAccelerometerValues = event.values.clone();

				// LPFで高調波ノイズ成分除去
				fltOrientX = (float)(fltOrientX * 0.9 + event.values[0] * 0.1);
				fltOrientY = (float)(fltOrientY * 0.9 + event.values[1] * 0.1);
				fltOrientZ = (float)(fltOrientZ * 0.9 + event.values[2] * 0.1);
				// 差分表示により重力加速度の影響を除去
				fltAccelX = event.values[0] - fltOrientX;
				fltAccelY = event.values[1] - fltOrientY;
				fltAccelZ = event.values[2] - fltOrientZ;

				if(boolAccelMaxHold == true){
					if(fltAccelX > fltAccelMaxX){
						fltAccelMaxX = fltAccelX;
					}
					if(fltAccelY > fltAccelMaxY){
						fltAccelMaxY = fltAccelY;
					}
					if(fltAccelZ > fltAccelMaxZ){
						fltAccelMaxZ = fltAccelZ;
					}
				}
				if(boolAccelMinHold == true){
					if(fltAccelX < fltAccelMinX){
						fltAccelMinX = fltAccelX;
					}
					if(fltAccelY < fltAccelMinY){
						fltAccelMinY = fltAccelY;
					}
					if(fltAccelZ < fltAccelMinZ){
						fltAccelMinZ = fltAccelZ;
					}
				}
				break;
			case Sensor.TYPE_MAGNETIC_FIELD:
				floatMagneticValues = event.values.clone();
				break;
			case Sensor.TYPE_GYROSCOPE:
				fltGyroX = event.values[0];
				fltGyroY = event.values[1];
				fltGyroZ = event.values[2];
				break;
			case Sensor.TYPE_ORIENTATION:
				fltAngleAzi = event.values[0];
				fltAnglePit = event.values[1];
				fltAngleRol = event.values[2];
				break;
		}

		if (floatMagneticValues != null && floatAccelerometerValues != null) {
			SensorManager.getRotationMatrix(floatInR, floatI, floatAccelerometerValues, floatMagneticValues);

			// Activityの表示が縦固定の場合。横向きになる場合、修正が必要です
			SensorManager.remapCoordinateSystem(floatInR, SensorManager.AXIS_X, SensorManager.AXIS_Z, floatOutR);
			SensorManager.getOrientation(floatOutR, floatOrientationValues);
			floatOrientationValuesLPF[0] = (float)(floatOrientationValuesLPF[0] * 0.9 + floatOrientationValues[0] * 0.1);
			floatOrientationValuesLPF[1] = (float)(floatOrientationValuesLPF[1] * 0.9 + floatOrientationValues[1] * 0.1);
			floatOrientationValuesLPF[2] = (float)(floatOrientationValuesLPF[2] * 0.9 + floatOrientationValues[2] * 0.1);
		}
	}
}

