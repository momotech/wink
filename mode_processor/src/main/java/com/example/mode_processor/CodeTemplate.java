package com.example.mode_processor;

import com.example.lib.Default;
import com.example.lib.ModeJudger;
import com.example.lib.ModeJudgerAnd;
import com.example.lib.ModeJudgerOR;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

public class CodeTemplate {

    public static String getLinkMode(int linkMode) {
//        return "null != getLiveData() && null != getLiveData().getProfile() && getLiveData().getProfile().getLink_model() == RoomProfile.LINK_MODEL_GUINNESS";
        String judgeSign = getJudgeSign(linkMode);
        linkMode = abs(linkMode);
        return "null != getLiveData() && null != getLiveData().getProfile() && getLiveData().getProfile().getLink_model() " + judgeSign + " " + linkMode;
    }

    // int 值 > 0 为 == , < 0 为 !=
    public static String getRTypeCode(int rType) {
//        return "null != getLiveData() && null != getLiveData().getProfile() && getLiveData().getProfile().getRtype() != RoomProfile.TYPE_GUINNESS_HOST";
        String judgeSign = getJudgeSign(rType);
        rType = abs(rType);
        return "null != getLiveData() && null != getLiveData().getProfile() && getLiveData().getProfile().getRtype() " + judgeSign + " " + rType;
    }

    public static String getSei(int seiType) {
//        val seiInfo = SeiUtil.convert(sei)
//        val seiType = SeiUtil.getType(seiInfo)
//        return seiType == SeiUtil.TYPE_LSGAME
        String judgeSign = getJudgeSign(seiType);
        seiType = abs(seiType);
        StringBuilder sb = new StringBuilder();
        sb.append("\ncom.immomo.molive.connect.bean.OnlineMediaPosition mediaPosition = com.immomo.molive.connect.utils.SeiUtil.convert(getPlayer().getLastSei());");
        sb.append("\nif (com.immomo.molive.connect.utils.SeiUtil.getType(mediaPosition) " + judgeSign + " ").append(seiType);//.append(") {");
        return sb.toString();
    }

    /*
        if (null != getLiveData() && null != getLiveData().getProfile() && null != getLiveData().getProfile().getArena() && getLiveData().getProfile().getArena().getType() == RoomProfile.PK_TYPE_ARENA) {
            return true;
        }
     */
    public static String getArenaTypeCode(int arenaType) {
        String judgeSign = getJudgeSign(arenaType);
        arenaType = abs(arenaType);
        return "null != getLiveData() && null != getLiveData().getProfile() && null != getLiveData().getProfile().getArena() && getLiveData().getProfile().getArena().getType() " + judgeSign + " " + arenaType;
    }

    /*
        if (null != liveData && null != liveData.profile && liveData.profile.fulltime_mode == RoomProfile.FULL_TIME_VIDEO) {
            // 24小时房
            if (liveData.profile.link_model == RoomProfile.LINK_MODEL_FT_PAL && liveData.profile.sub_mode == FTPAL_V2_FLAG) {
                return true
            }
        }
        return false
     */
    public static String getFullTimeModeCode(int fullTimeMode) {
        String judgeSign = getJudgeSign(fullTimeMode);
        fullTimeMode = abs(fullTimeMode);
        return "null != getLiveData() && null != getLiveData().getProfile() && getLiveData().getProfile().getFulltime_mode() " + judgeSign + " " + fullTimeMode;
    }

    public static String getSubModeCode(int subMode) {
        String judgeSign = getJudgeSign(subMode);
        subMode = abs(subMode);
        return "null != getLiveData() && null != getLiveData().getProfile() && getLiveData().getProfile().getSub_mode() " + judgeSign + " " + subMode;
    }

    /*
        if (liveData?.profile?.teamPkType == RoomProfile.PK_TYPE_TEAM_FIGHT && judged(player.lastSei)) {
            return true
        }
     */
    public static String  getPKTypeCode(int pkType) {
        String judgeSign = getJudgeSign(pkType);
        pkType = abs(pkType);
        return "null != getLiveData() && null != getLiveData().getProfile() && getLiveData().getProfile().getTeamPkType() " + judgeSign + " " + pkType;
    }

    public static String getSRCNotInCode(String[] srcArr) {
        if (srcArr == null || srcArr.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("null != getLiveData() && null != getLiveData().getSrc() && (");
        for (int i = 0; i < srcArr.length; i++) {
            if (i != 0) {
                sb.append(" && ");
            }
            sb.append("!android.text.TextUtils.equals(getLiveData().getSrc(), \"").append(srcArr[i]).append("\")");
        }
        sb.append(")");
        return sb.toString();
    }

    public static String getSRCCode(String[] srcArr) {
        if (srcArr == null || srcArr.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("null != getLiveData() && null != getLiveData().getSrc() && (");
        for (int i = 0; i < srcArr.length; i++) {
            if (i != 0) {
                sb.append(" || ");
            }
            sb.append("android.text.TextUtils.equals(getLiveData().getSrc(), \"").append(srcArr[i]).append("\")");
        }
        sb.append(")");
        return sb.toString();
    }

    /**
     * 单组/与/或
     */
    public static String getCode(Element routeElement, int type, Messager messager) {
        messager.printMessage(Diagnostic.Kind.NOTE, "========");
        StringBuilder sb = new StringBuilder();
        if (type == Type.TYPE_MODE_JUDGER) {
            ModeJudger modeJudger = routeElement.getAnnotation(ModeJudger.class);
            addModeJudgerCode(modeJudger, sb);
        } else if (type == Type.TYPE_MODE_JUDGER_AND) {
            StringBuilder subSb = new StringBuilder();
            ModeJudgerAnd modeJudgerAnd = routeElement.getAnnotation(ModeJudgerAnd.class);
            if (modeJudgerAnd == null) {
                return "";
            }
            for (ModeJudger modeJudger : modeJudgerAnd.value()) {
                sb.append("\n//===========================\n");
                addModeJudgerCode(modeJudger, subSb);
                sb.append(subSb.toString());
                subSb = new StringBuilder();
            }
        } else if (type == Type.TYPE_MODE_JUDGER_OR) {
            StringBuilder subSb = new StringBuilder();
            ModeJudgerOR modeJudgerOr = routeElement.getAnnotation(ModeJudgerOR.class);
            if (modeJudgerOr == null) {
                return "";
            }
//            for (ModeJudger modeJudger : modeJudgerOr.value()) {
//                sb.append("\n//===========================\n");
//                addModeJudgerCode(modeJudger, subSb);
//                sb.append(subSb.toString());
//                subSb = new StringBuilder();
//            }
        }
        return sb.toString();
    }

    public static String getJudgeSeiCode(int seiType) {
        return
                "int type = com.immomo.molive.connect.utils.SeiUtil.getType(sei);\n" +
                        "return " + seiType + " == type";
    }

    public static String getSeiCallbackCode(int seiType) {
        return
                "\n" +
                "if (mModeJudgerEventListener == null) {\n" +
                "    return;\n" +
                "}\n" +
                "if (getCurrentController() == null || getCurrentController() != null && getCurrentComponentCreator() != this) {\n" +
                "    if (judge(sei)) {\n" +
                "        mModeJudgerEventListener.onEvent(this.getLiveMode());\n" +
                "    }\n" +
                "} else if (getCurrentController() != null && getCurrentComponentCreator() == this){\n" +
                "    if (!judge(sei)) {\n" +
                "        mModeJudgerEventListener.onEventSwitchByProfile();\n" +
                "    }\n" +
                "}";
    }

    private static int addModeJudgerCode(ModeJudger modeJudger, StringBuilder sb) {
        int conditionCount = 0;
        if (modeJudger.seiType() != Default.INT) {
            conditionCount++;
//            sb.append("\n//SEI\n");
            sb.append(getSei(modeJudger.seiType()));
        }
        if (modeJudger.linkMode() != Default.INT) {
//            sb.append("\n//LINK_MODE\n");
            insertAndOrIf(sb, conditionCount);
            sb.append(getLinkMode(modeJudger.linkMode()));
            conditionCount++;
        }
        if (modeJudger.arenaType() != Default.INT) {
            insertAndOrIf(sb, conditionCount);
            sb.append(getArenaTypeCode(modeJudger.arenaType()));
            conditionCount++;
        }
        if (modeJudger.subMode() != Default.INT) {
//            sb.append("\n//SUB_MODE\n");
            insertAndOrIf(sb, conditionCount);
            sb.append(getSubModeCode(modeJudger.subMode()));
            conditionCount++;
        }
        if (modeJudger.src().length != 0) {
            insertAndOrIf(sb, conditionCount);
            sb.append(getSRCCode(modeJudger.src()));
            conditionCount++;
        }
        if (modeJudger.srcExclude().length != 0) {
            insertAndOrIf(sb, conditionCount);
            sb.append(getSRCNotInCode(modeJudger.srcExclude()));
            conditionCount++;
        }
        if (modeJudger.rType() != Default.INT) {
//            sb.append("\n//RTYPE\n");
            insertAndOrIf(sb, conditionCount);
            sb.append(getRTypeCode(modeJudger.rType()));
            conditionCount++;
        }
        if (modeJudger.pkType() != Default.INT) {
//            sb.append("\n//PK_TYPE\n");
            insertAndOrIf(sb, conditionCount);
            sb.append(getPKTypeCode(modeJudger.pkType()));
            conditionCount++;
        }
        if (modeJudger.fullTimeMode() != Default.INT) {
//            sb.append("\n//FULL_TIME_MODE\n");
            insertAndOrIf(sb, conditionCount);
            sb.append(getFullTimeModeCode(modeJudger.fullTimeMode()));
            conditionCount++;
        }
        if (conditionCount > 0) {
            sb.append(") {");
            sb.append("\n    return true;");
            sb.append("\n}");
        }
        return conditionCount;
    }

    private static void insertAndOrIf(StringBuilder sb, int conditionCount) {
        if (conditionCount > 0) {
            sb.append(" \n    && ");
        } else {
            sb.append("if (");
        }
    }

    private static void loggerInfo(Messager messager, String msg) {
        messager.printMessage(Diagnostic.Kind.NOTE, msg);
    }

    private static String getJudgeSign(int opCode) {
        String judgeSign = "==";
        if (opCode < 0) {
            judgeSign = "!=";
        }
        return judgeSign;
    }

    private static int abs(int code) {
        return Math.abs(code);
    }

}
