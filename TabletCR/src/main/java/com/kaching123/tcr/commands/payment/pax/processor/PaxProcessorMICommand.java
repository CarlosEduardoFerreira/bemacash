package com.kaching123.tcr.commands.payment.pax.processor;

import com.kaching123.tcr.model.PaxModel;
import com.telly.groundy.TaskResult;

/**
 * Created by mayer
 */
public class PaxProcessorMICommand extends PaxProcessorBaseCommand {

    @Override
    protected TaskResult doCommand() {
        return succeeded();
    }

    @Override
    protected PaxModel getPaxModel() {
        return (PaxModel) getArgs().getParcelable(ARG_DATA_PAX);
    }

}
