package makonon.fishingsimulator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class FishingGraphView extends View
{
	int PosX[] = new int[36000];
	int PosY[] = new int[36000];
	int PosBtm[] = new int[100];
	int counter = 0;

	public void setLurePos(double LureX, double LureY){
		PosX[counter] = (int)(Math.round(LureX * 10));
		PosY[counter] = (int)(Math.round(LureY * 10));
		counter = counter + 1;
	}

	public void initLurePos(){
		for ( int i = 0; i < 36000 - 1 ; i++ ){
			PosX[i] = 0;
			PosY[i] = 0;
			counter = 0;
		}
	}

	public void setBtmPos(int X,double BottomDepth){
		PosBtm[X] = (int)(Math.round(BottomDepth * 10));
	}

	public FishingGraphView(Context context) {
		super(context);
	}

	public FishingGraphView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public FishingGraphView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		// 座標グリッドの生成

		// Viewの幅、高さを知る
		int width = this.getWidth();
		int height = this.getHeight();
		// グラフの中心線を求める。
		int base = height * 2 / 5;
		// Viewで描画を行うためのクラスをインスタンス化。
		Paint paint = new Paint();
		// 色指定（アルファチャネル、赤、緑、青）
		paint.setColor(Color.argb(75, 255, 255, 255));
		// 線の太さ
		paint.setStrokeWidth(1);
		// グリッドの縦線描画
		for (int y = 0; y < height; y = y + 10) {
			canvas.drawLine(0, y, width, y, paint);
		}
		// グリッドの横線描画
		for (int x = 0; x < width; x = x + 10) {
			canvas.drawLine(x, 0, x, height, paint);
		}
		// 中心線を赤で描画
		paint.setColor(Color.RED);
		canvas.drawLine(0, base, width, base, paint);

		// bottomをwhite線で表示
		paint.setColor(Color.WHITE);
		paint.setStrokeWidth(2);
		for ( int i = 0; i < 100 - 1; i++ ){
			canvas.drawLine(i*10, base - PosBtm[i],(i+1)*10,base - PosBtm[i + 1], paint);
		}

		// グラフを黄色線で表示
		paint.setColor(Color.YELLOW);
		paint.setStrokeWidth(2);
		for ( int i = 0; i < counter - 1 ; i++ ){
			canvas.drawLine(PosX[i], base - PosY[i],PosX[i+1],base - PosY[i + 1], paint);
		}
	}

	/*
	 float div =0;
	 int counter = 0;
	 int[] counterhistory = new int[1080];

	 public void setDiv(float div){
	 this.div = div;
	 this.counter++;
	 if (counterhistory.length <= counter){
	 counter = 0;
	 }
	 counterhistory[counter] = (int)div * 5;
	 }

	 public FishingGraphView(Context context) {
	 super(context);
	 }

	 public FishingGraphView(Context context, AttributeSet attrs, int defStyle) {
	 super(context, attrs, defStyle);
	 }

	 public FishingGraphView(Context context, AttributeSet attrs) {
	 super(context, attrs);
	 }

	 @Override
	 protected void onDraw(Canvas canvas) {
	 super.onDraw(canvas);

	 // 座標グリッドの生成

	 // Viewの幅、高さを知る
	 int width = this.getWidth();
	 int height = this.getHeight();
	 // グラフの中心線を求める。
	 int base = height/2;
	 // Viewで描画を行うためのクラスをインスタンス化。
	 Paint paint = new Paint();
	 // 色指定（アルファチャネル、赤、緑、青）
	 paint.setColor(Color.argb(75, 255, 255, 255));
	 // 線の太さ
	 paint.setStrokeWidth(1);
	 // グリッドの縦線描画
	 for (int y = 0; y < height; y = y + 10) {
	 canvas.drawLine(0, y, width, y, paint);
	 }
	 // グリッドの横線描画
	 for (int x = 0; x < width; x = x + 10) {
	 canvas.drawLine(x, 0, x, height, paint);
	 }
	 // 中心線を赤で描画
	 paint.setColor(Color.RED);
	 canvas.drawLine(0, base, width, base, paint);


	 // グラフを黄色線で表示
	 paint.setColor(Color.YELLOW);
	 paint.setStrokeWidth(2);
	 for ( int i = 0; i < counterhistory.length - 1 ; i++ ){
	 canvas.drawLine(i, base + counterhistory[i], i + 1, 
	 base + counterhistory[i + 1], paint);
	 }

	 // 現在を赤色縦線で表示
	 paint.setColor(Color.RED);
	 canvas.drawLine(counter, 0, counter, height, paint);

	 Log.v("GraphView","counter:"+counter);
	 }
	 */
	
}
