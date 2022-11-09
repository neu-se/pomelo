package edu.neu.ccs.prl.pomelo.fuzz;

import edu.neu.ccs.prl.meringue.Replayer;
import edu.neu.ccs.prl.meringue.ZestFramework;

public class PomeloFuzzFramework extends ZestFramework {
    @Override
    public Class<? extends Replayer> getReplayerClass() {
        return PomeloReplayer.class;
    }

    public String getMainClassName() {
        return FuzzForkMain.class.getName();
    }

    @Override
    public String getCoordinate() {
        return "edu.neu.ccs.prl.pomelo:pomelo-meringue-extension";
    }
}
