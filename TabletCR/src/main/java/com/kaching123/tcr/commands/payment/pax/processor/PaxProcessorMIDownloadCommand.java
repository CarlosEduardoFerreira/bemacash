package com.kaching123.tcr.commands.payment.pax.processor;

import com.kaching123.tcr.model.PaxModel;
import com.telly.groundy.TaskResult;

/**
 * Created by pkabakov on 25.06.2014.
 */
public class PaxProcessorMIDownloadCommand extends PaxProcessorBaseCommand {

    @Override
    protected TaskResult doCommand() {
        return succeeded();
    }

    @Override
    protected PaxModel getPaxModel() {
        return (PaxModel) getArgs().getSerializable(ARG_DATA_PAX);
    }


}
