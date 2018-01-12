LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := avcodec-56-prebuilt
LOCAL_SRC_FILES := prebuilt/libavcodec-55.so
include $(PREBUILT_SHARED_LIBRARY)




include $(CLEAR_VARS)
LOCAL_MODULE := avformat-56-prebuilt
LOCAL_SRC_FILES := prebuilt/libavformat-55.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := avutil-54-prebuilt
LOCAL_SRC_FILES := prebuilt/libavutil-52.so
include $(PREBUILT_SHARED_LIBRARY)


include $(CLEAR_VARS)
LOCAL_MODULE := swscale-3-prebuilt
LOCAL_SRC_FILES := prebuilt/libswscale-2.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)

LOCAL_MODULE := client1
LOCAL_SRC_FILES := 	myjni.cpp\
					MyFFmpeg.cpp\
					MyQueue.c

LOCAL_LDLIBS := -llog -ljnigraphics -lz -landroid
LOCAL_SHARED_LIBRARIES := avcodec-56-prebuilt   avformat-56-prebuilt avutil-54-prebuilt swscale-3-prebuilt

include $(BUILD_SHARED_LIBRARY)