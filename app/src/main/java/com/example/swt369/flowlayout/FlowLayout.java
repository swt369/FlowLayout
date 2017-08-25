package com.example.swt369.flowlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by swt369 on 2017/8/25.
 */

public class FlowLayout extends ViewGroup {
    /**
     * left-aligned
     */
    public static final int ALIGNMENT_LEFT = 0;
    /**
     * center-aligned
     */
    public static final int ALIGNMENT_CENTER = 1;
    /**
     * right-aligned
     */
    public static final int ALIGNMENT_RIGHT = 2;

    private int mAlignment;

    private int mSpaceLeftAndRight;
    private int mSpaceTopAndBottom;
    private int mSpaceBetweenChildren;
    private int mSpaceBetweenLevels;

    private int mWidth;
    private int mHeight;

    boolean mHasSplitLines;
    Paint paintForSplitLines;
    private static final int DEFAULT_SPLIT_LINE_COLOR = Color.argb(255,176,48,96);
    private static final int DEFAULT_SPLIT_LINE_THICKNESS = 4;


    private ArrayList<ArrayList<View>> mLevels;
    private ArrayList<Integer> mLevelHeights;

    public FlowLayout(Context context) {
        this(context,null);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray ta = context.obtainStyledAttributes(attrs,R.styleable.FlowLayout);
        try {
            mAlignment = ta.getInt(R.styleable.FlowLayout_alignment,ALIGNMENT_LEFT);
            mHasSplitLines = ta.getBoolean(R.styleable.FlowLayout_hasSplitLines,true);
        }finally {
            ta.recycle();
        }

        float density = context.getResources().getDisplayMetrics().density;
        mSpaceLeftAndRight = (int)(10 * density);
        mSpaceTopAndBottom = (int)(5 * density);
        mSpaceBetweenChildren = (int)(5 * density);
        mSpaceBetweenLevels = (int)(5 * density);

        paintForSplitLines = new Paint();
        paintForSplitLines.setColor(DEFAULT_SPLIT_LINE_COLOR);
        paintForSplitLines.setStrokeWidth(DEFAULT_SPLIT_LINE_THICKNESS);
    }

    public void setAlignment(int alignment){
        if(alignment == mAlignment){
            return;
        }
        if(alignment == ALIGNMENT_LEFT || alignment == ALIGNMENT_CENTER || alignment == ALIGNMENT_RIGHT){
            mAlignment = alignment;
            requestLayout();
        }
    }

    public int getAlignment(){
        return mAlignment;
    }

    public void openSplitLines(){
        if(!mHasSplitLines){
            mHasSplitLines = true;
            invalidate();
        }
    }

    public void closeSplitLines(){
        if(mHasSplitLines){
            mHasSplitLines = false;
            invalidate();
        }
    }

    public boolean hasSplitLines(){
        return mHasSplitLines;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        //step 1,determine the width of this layout.
        //make the width equal widthSize whatever the width mode is.
        if(widthMode == MeasureSpec.EXACTLY || widthMode == MeasureSpec.AT_MOST || widthMode == MeasureSpec.UNSPECIFIED){
            mWidth = widthSize;
        }

        //step 2,measure all the children in order to get their width and height
        int count = getChildCount();
        int[] childWidths = new int[count];
        for(int i = 0 ; i < count ; i++){
            View child = getChildAt(i);
            if(child.getVisibility() != GONE){
                measureChildWithMargins(
                        child,
                        widthMeasureSpec,2 * mSpaceLeftAndRight,
                        heightMeasureSpec,2 * mSpaceTopAndBottom
                );
//                child.measure(
//                        getChildMeasureSpec(widthMeasureSpec,2 * mSpaceLeftAndRight,child.getLayoutParams().width),
//                        getChildMeasureSpec(heightMeasureSpec,2 * mSpaceTopAndBottom,child.getLayoutParams().height)
//                );
                int height = child.getMeasuredHeight();
                childWidths[i] = child.getMeasuredWidth();
            }
        }

        //step 3,determine which level the children will be in.
        mLevels = new ArrayList<>();
        mLevels.add(new ArrayList<View>());
        int curLevel = 0;
        int curX = mSpaceLeftAndRight;
        for(int i = 0 ; i < count ; i++){
            View child = getChildAt(i);
            if(child.getVisibility() != GONE){
                if(childWidths[i] > mWidth - 2 * mSpaceLeftAndRight){
                    //this view is too big to be put in even an empty level,so give it up.
                    continue;
                }
                if(curX + childWidths[i] <= mWidth - mSpaceLeftAndRight){
                    //current level has enough space to put this view in.
                    mLevels.get(curLevel).add(child);
                    curX += (childWidths[i] + mSpaceBetweenChildren);
                }else {
                    //current level doesn't have enough space to add this view,
                    //so switch into next level.
                    mLevels.add(new ArrayList<View>());
                    curLevel++;
                    curX = mSpaceLeftAndRight;
                    mLevels.get(curLevel).add(child);
                    curX += (childWidths[i] + mSpaceBetweenChildren);

                }
                if(curX > mWidth - mSpaceLeftAndRight){
                    //current level doesn't have enough space to add any view,
                    //so switch into next level.
                    mLevels.add(new ArrayList<View>());
                    curLevel++;
                    curX = mSpaceLeftAndRight;
                }
            }
        }

        //step 4,adjust the height of the children according to their layout_height.
        mLevelHeights = new ArrayList<>();
        for(int i = 0 ; i < mLevels.size() ; i++){
            //obtain the maximum height of this level
            int maxHeight = 0;
            for(View child : mLevels.get(i)){
                if(child.getVisibility() != GONE){
                    maxHeight = Math.max(maxHeight,child.getMeasuredHeight());
                }
            }
            mLevelHeights.add(maxHeight);
            //if a child's layout_height equals MATCH_PARENT
            //and its height doesn't equal the maximum height of this level,
            //then remeasure it.
            for(View child : mLevels.get(i)){
                if(child.getVisibility() != GONE){
                    if(child.getLayoutParams().height == ViewGroup.LayoutParams.MATCH_PARENT
                            && child.getMeasuredHeight() != maxHeight){
                        child.measure(
                                MeasureSpec.makeMeasureSpec(child.getMeasuredWidth(),MeasureSpec.EXACTLY),
                                MeasureSpec.makeMeasureSpec(maxHeight,MeasureSpec.EXACTLY)
                        );
                    }
                }
            }
        }

        //step 5,determine the height of this layout.
        //be the size of the parent
        if(heightMode == MeasureSpec.EXACTLY){
            mHeight = heightSize;
        }else {
            //be the size of all the levels.
            mHeight = 2 * mSpaceTopAndBottom;
            for(Integer integer : mLevelHeights){
                mHeight += (integer + mSpaceBetweenLevels);
            }
            mHeight -= mSpaceBetweenLevels;
        }

        //step 6,set the width and height of this layout.
        setMeasuredDimension(mWidth,mHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int curY = mSpaceTopAndBottom;
        for(int i = 0 ; i < mLevels.size() ; i++){
            //calculate the total width of the views in this level(including space between them).
            int curX = 0;
            int totalWidth = 0;
            for(View child : mLevels.get(i)){
                totalWidth += (child.getMeasuredWidth() + mSpaceBetweenChildren);
            }
            totalWidth -= mSpaceBetweenChildren;
            //use different ways to determine the start position
            //so as to realize different ways of alignment.
            switch (mAlignment){
                case ALIGNMENT_LEFT:
                    curX = mSpaceLeftAndRight;
                    break;
                case ALIGNMENT_CENTER:
                    curX = (mWidth - totalWidth) / 2;
                    break;
                case ALIGNMENT_RIGHT:
                    curX = mWidth - mSpaceLeftAndRight - totalWidth;
                    break;
            }
            //determine the accurate position of the views according to their layout_gravity
            for(View view : mLevels.get(i)){
                if(view.getVisibility() != GONE){
                    LayoutParams params = (FlowLayout.LayoutParams)view.getLayoutParams();
                    switch (params.gravity){
                        case LayoutParams.GRAVITY_TOP:
                            view.layout(
                                    curX,
                                    curY,
                                    curX + view.getMeasuredWidth(),
                                    curY + view.getMeasuredHeight()
                            );
                            break;
                        case LayoutParams.GRAVITY_CENTER:
                            int space = (mLevelHeights.get(i) - view.getMeasuredHeight()) / 2;
                            view.layout(
                                    curX,
                                    curY + space,
                                    curX + view.getMeasuredWidth(),
                                    curY + mLevelHeights.get(i) - space
                            );
                            break;
                        case LayoutParams.GRAVITY_BOTTOM:
                            view.layout(
                                    curX,
                                    curY + mLevelHeights.get(i) - view.getMeasuredHeight(),
                                    curX + view.getMeasuredWidth(),
                                    curY + mLevelHeights.get(i)
                            );
                            break;
                    }
                    curX += (view.getMeasuredWidth() + mSpaceBetweenChildren);
                }
            }
            curY += (mLevelHeights.get(i) + mSpaceBetweenLevels);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(mHasSplitLines){
            //draw split lines
            int curY = mSpaceTopAndBottom / 2;
            for(int i = 0 ; i < mLevelHeights.size() ; i++){
                canvas.drawLine(0,curY,mWidth,curY,paintForSplitLines);
                curY += (mLevelHeights.get(i) + mSpaceBetweenLevels);
            }
            canvas.drawLine(0,curY,mWidth,curY,paintForSplitLines);
        }
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof FlowLayout.LayoutParams;
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p.width,p.height);
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(),attrs);
    }

    public static class LayoutParams extends ViewGroup.MarginLayoutParams{
        public static final int GRAVITY_TOP = 0;
        public static final int GRAVITY_CENTER = 1;
        public static final int GRAVITY_BOTTOM = 2;
        public int gravity = GRAVITY_BOTTOM;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);

            TypedArray ta = c.obtainStyledAttributes(attrs,R.styleable.FlowLayout_Layout);
            try {
                gravity = ta.getInt(R.styleable.FlowLayout_Layout_layout_gravity,GRAVITY_CENTER);
            }finally {
                ta.recycle();
            }
        }

        public LayoutParams(int width,int height) {
            super(width, height);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }
}
