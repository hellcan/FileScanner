package com.example.fengcheng.main.filescanner;

import android.support.annotation.NonNull;

/**
 * @Package com.example.fengcheng.main.filescanner
 * @FileName FileInfo
 * @Date 4/8/18, 1:58 AM
 * @Author Created by fengchengding
 * @Description FileScanner
 */

public class FileInfo {

    private String fileName;
    private long fileSize;
//    private String extension;

    public FileInfo(String fileName, long fileSize) {
        this.fileName = fileName;
        this.fileSize = fileSize;
//        this.extension = extension;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

}
