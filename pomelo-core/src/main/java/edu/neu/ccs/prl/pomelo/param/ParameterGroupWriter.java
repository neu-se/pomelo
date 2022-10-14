package edu.neu.ccs.prl.pomelo.param;

import java.io.File;
import java.io.IOException;

public interface ParameterGroupWriter {
    void write(File source, Object[] parameterGroup) throws IOException;
}
