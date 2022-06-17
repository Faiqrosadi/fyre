package com.fyre.util.judge;

import com.fyre.util.judge.checkers.Checker;

import java.io.File;

public class SubmissionHandler {

    public boolean isAccepted(File judgeOutput, String participantOutput, Checker checker) {
        return checker.compare(judgeOutput, participantOutput);
    }

}
