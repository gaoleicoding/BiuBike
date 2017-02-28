/*
package com.biubike.map;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.Projection;
import com.baidu.mapapi.model.inner.Point;

*/
/**
 * Created by gaolei on 17/2/27.
 *//*


class RouteLineOverlay extends Overlay
{
    @Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
        // TODO Auto-generated method stub
        super.draw(canvas, mapView, shadow);
        Projection projection=mapView.getP
        Point p1=new Point();
        Point p2=new Point();
        Point p3=new Point();

        //经度转像素
        projection.toPixels(gPoint1, p1);
        projection.toPixels(gPoint2, p2);
        projection.toPixels(gPoint3, p3);

        //第一个画笔   画圆
        Paint fillPaint=new Paint();
        fillPaint.setColor(Color.BLUE);
        fillPaint.setAntiAlias(true);
        fillPaint.setStyle(Paint.Style.FILL);

        //将图画到上层
        canvas.drawCircle(p1.x, p1.y, 5.0f, fillPaint);
        canvas.drawCircle(p2.x, p2.y, 5.0f, fillPaint);
        canvas.drawCircle(p3.x, p3.y, 5.0f, fillPaint);

        //第二个画笔  画线
        Paint paint=new Paint();
        paint.setColor(Color.BLUE);
        paint.setDither(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(4);

        //连线
        Path path=new Path();
        path.moveTo(p1.x, p1.y);
        path.lineTo(p2.x, p2.y);
        path.lineTo(p3.x, p3.y);

        //画出路径
        canvas.drawPath(path, paint);
    }
}
*/
