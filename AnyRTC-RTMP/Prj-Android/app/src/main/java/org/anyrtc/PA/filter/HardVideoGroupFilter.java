package org.anyrtc.PA.filter;

import android.opengl.GLES20;

import org.anyrtc.PA.gles.GLESTools;

import java.nio.FloatBuffer;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by kanli on 12/19/16.
 */

public class HardVideoGroupFilter extends BaseHardVideoFilter {
    private LinkedList<FilterWrapper> filterWrappers;

    public HardVideoGroupFilter(List<BaseHardVideoFilter> filters) {
        if (filters == null || filters.isEmpty()) {
            throw new IllegalArgumentException("can not create empty GroupFilter");
        }
        filterWrappers = new LinkedList<FilterWrapper>();
        for (BaseHardVideoFilter filter : filters) {
            filterWrappers.add(new FilterWrapper(filter));
        }
    }

    @Override
    public void onInit(int inWidth, int inHeight, int outWidth, int outHeight) {
        super.onInit(inWidth, inHeight, outWidth, outHeight);
        int i = 0;
        for (FilterWrapper wrapper : filterWrappers) {
            wrapper.filter.onInit(inWidth, inHeight, outWidth, outHeight);
            int[] frameBuffer = new int[1];
            int[] frameBufferTexture = new int[1];
            GLESTools.createFrameBuff(frameBuffer,
                    frameBufferTexture,
                    IN_WIDTH,
                    IN_HEIGHT);
            wrapper.frameBuffer = frameBuffer[0];
            wrapper.frameBufferTexture = frameBufferTexture[0];
            i++;
        }
    }


    @Override
    public void onDraw(int cameraTexture, int targetFrameBuffer, FloatBuffer shapeBuffer, FloatBuffer textrueBuffer) {
        FilterWrapper preFilterWrapper = null;
        int i = 0;
        int texture;
        for (FilterWrapper wrapper : filterWrappers) {
            if (preFilterWrapper == null) {
                texture = cameraTexture;
            } else {
                texture = preFilterWrapper.frameBufferTexture;
            }
            if (i == (filterWrappers.size() - 1)) {
                wrapper.filter.onDraw(texture, targetFrameBuffer, shapeBuffer, textrueBuffer);
            } else {
                wrapper.filter.onDraw(texture, wrapper.frameBuffer, shapeBuffer, textrueBuffer);
            }
            preFilterWrapper = wrapper;
            i++;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for (FilterWrapper wrapper : filterWrappers) {
            wrapper.filter.onDestroy();
            GLES20.glDeleteFramebuffers(1, new int[]{wrapper.frameBuffer}, 0);
            GLES20.glDeleteTextures(1, new int[]{wrapper.frameBufferTexture}, 0);
        }
    }

    @Override
    public void onDirectionUpdate(int _directionFlag) {
        super.onDirectionUpdate(_directionFlag);
        for (FilterWrapper wrapper : filterWrappers) {
            wrapper.filter.onDirectionUpdate(_directionFlag);
        }
    }

    private class FilterWrapper {
        BaseHardVideoFilter filter;
        int frameBuffer;
        int frameBufferTexture;

        FilterWrapper(BaseHardVideoFilter filter) {
            this.filter = filter;
        }
    }
}