package com.alibaba.otter.shared.arbitrate.impl.setl.memory;

import com.alibaba.otter.shared.common.model.config.enums.StageType;

public class MemoryStageControllerTest {

    static MemoryStageController mc = new MemoryStageController(1L);


    static void d(){
        try {
            mc.waitForProcess(StageType.SELECT);

            mc.waitForProcess(StageType.SELECT);
            mc.waitForProcess(StageType.SELECT);
            mc.waitForProcess(StageType.SELECT);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {

    }
}
