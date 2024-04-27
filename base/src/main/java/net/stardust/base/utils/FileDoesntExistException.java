package net.stardust.base.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.StandardException;

import java.io.File;

@Getter
@AllArgsConstructor
@StandardException
public class FileDoesntExistException extends RuntimeException {

    private File file;

}
