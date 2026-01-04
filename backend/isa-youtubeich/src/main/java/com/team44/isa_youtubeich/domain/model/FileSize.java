package com.team44.isa_youtubeich.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.io.Serializable;

@Embeddable
public class FileSize implements Serializable {

    private static final long serialVersionUID = 1L;

    @Getter
    private Long bytes;

    private FileSize() { }

    private FileSize(Long bytes){
        this.bytes = bytes;
    }

    public static FileSize of(Long bytes) throws IllegalArgumentException {
        if(bytes < 0){
            throw new IllegalArgumentException(String.format("Invalid file size: %d", bytes));
        }
        return new FileSize(bytes);
    }

    public static FileSize of(Long size, FileSizeUnit unit) throws IllegalArgumentException {

        if(size < 0){
            throw new IllegalArgumentException(String.format("Invalid file size: %d", size));
        }

        switch(unit){
            case B -> {
                return new FileSize(size);
            }
            case KB -> {
                return new FileSize(size * 1024);
            }
            case MB -> {
                return new FileSize(size * 1024 * 1024 );
            }
            case GB -> {
                return new FileSize(size * 1024 * 1024 * 1024);
            }
        }
        throw new IllegalArgumentException();
    }

    @Override
    public String toString(){
        if(bytes < 1024) return bytes.toString() + " B";
        if(bytes < 1024 * 1024) return bytes.toString() + " KB";
        if(bytes < 1024 * 1024 * 1024) return bytes.toString() + " MB";
        return bytes.toString() + " GB";
    }

}
