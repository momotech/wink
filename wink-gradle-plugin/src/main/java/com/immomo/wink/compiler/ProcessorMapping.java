package com.immomo.wink.compiler;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public class ProcessorMapping implements Serializable {

    private static final long serialVersionUID = 1L;

    public HashMap<String, List<String>> annotation2FilesMapping = new HashMap<String, List<String>>();
    public HashMap<String, List<String>> file2AnnotationsMapping = new HashMap<String, List<String>>();
}