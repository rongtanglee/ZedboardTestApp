cmake_minimum_required(VERSION 3.4.1)

add_library(pl-jni SHARED
            pl-jni.c)

# Include libraries needed for hello-jni lib
target_link_libraries(pl-jni
                      android
                      log)