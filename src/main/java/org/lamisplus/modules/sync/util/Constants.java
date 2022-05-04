package org.lamisplus.modules.sync.util;

import org.springframework.stereotype.Component;

@Component
public class Constants {

    public static class ArtStatus {
        public static String ART_STATUS_UNKNOWN = "Unknown";
        public static String ART_STATUS_NEGATIVE = "HIV Negative";
        public static String ART_STATUS_POSITIVE = "HIV+ non ART";
        public static String PRE_ART_TRANSFER_OUT = "Pre-ART Transfer In";
        public static String PRE_ART_TRANSFER_IN = "Pre-ART Transfer Out";
        public static String ART_TRANSFER_OUT = "ART Transfer In";
        public static String ART_TRANSFER_IN = "ART Transfer Out";
        public static String ART_START = "ART Start";
        public static String ART_RESTART = "ART Restart";
        public static String ART_LOST = "Lost to Follow Up";
        public static String ART_STOP = "Stopped Treatment";
        public static String ART_DEAD = "Known Death";
    }

    public static class TxMlStatus {

        public static String TX_ML_DIED = "Died (Confirmed)";
        public static String TX_ML_TRANSFER = "Previously Undocumented Patient Transfer (Confirmed)";
        public static String TX_ML_AGREED_RETURN = "Traced and Agreed to Return to Care";
        public static String TX_ML_TRACED = "Traced Patient (Unable to locate)";
        public static String TX_ML_NOT_TRACED = "Did Not Attempt to Trace Patient";
    }

    public static class CauseDeath {

        public static String DEATH_TB = "HIV disease resulting in TB";
        public static String DEATH_CANCER = "HIV disease resulting in cancer";
        public static String DEATH_INFECTION = "HIV disease resulting in other infectious and parasitic disease";
        public static String DEATH_CONDITION = "Other HIV disease resulting in other disease or conditions leading to death";
        public static String DEATH_NATURAL = "Other natural causes";
        public static String DEATH_NON_NATURAL = "Non-natural causes";
        public static String DEATH_UNKNOWN = "Unknown cause";
    }

    public static class TypeVL {

        public static String VL_BASELINE = "Baseline";
        public static String VL_SECOND = "Second";
        public static String VL_ROUTINE = "Routine";
        public static String VL_REPEAT = "Repeat";
    }

    public static class CaseManager {

        public static Integer STABLE_ONE_YEAR = 1;
        public static Integer UNSTABLE_NOT_ONE_YEAR = 2;
        public static Integer UNSTABLE_ONE_YEAR = 3;
        public static Integer PRE_ART = 4;
    }

    public static class Prescription {

        public static Integer PRESCRIBED = 0;
        public static Integer PRESCRIBED_DISPENSED = 1;
        public static Integer PRESCRIBED_NOT_DISPENSED = 2;
    }

    public static class LTFU{

        public static Integer PEPFAR = 28;
        public static Integer GON = 90;
    }

    public static class ClientStatus {

        public static String NON_ART = "'HIV+ non ART', 'Pre-ART Transfer In'";
        public static String ON_CARE = "'HIV+ non ART', 'Pre-ART Transfer In','ART Start', 'ART Restart', 'ART Transfer In'";
        public static String ON_TREATMENT = "'ART Start', 'ART Restart', 'ART Transfer In'";
        public static String LOSSES = "'Pre-ART Transfer Out', 'ART Transfer Out', 'Lost to Follow Up', 'Stopped Treatment', 'Known Death'";
    }

    public static class Pmtct {

        public static class Child {

            public static String WITH_MOTHER = "WITH_MOTHER";
            public static String WITHOUT_MOTHER = "WITHOUT_MOTHER";
        }
    }

    public static class Tables {

        public static String TRANSACTION_TABLES = "monitor,user,casemanager,patient,clinic,pharmacy,laboratory,statushistory,regimenhistory,adrhistory,oihistory,adherehistory,chroniccare,dmscreenhistory,tbscreenhistory,anc,delivery,child,childfollowup,maternalfollowup,partnerinformation,specimen,eid,labno,nigqual,devolve,patientcasemanager,eac,motherinformation,biometric,assessment,hts,indexcontact";
        public static String MINIMUM_TRANSACTION_TABLES = "monitor,user,patient,clinic,pharmacy,laboratory,statushistory,regimenhistory";
        public static String SYSTEM_TABLES = "item,drug,regimentype,regimen,regimendrug,labtestcategory,labtest,state,lga,facility,communitypharm";
        public static String AUXILLARY_TABLES = "assessment,hts,indexcontact,patient,clinic,encounter,appointment,drugtherapy,mhtc";
    }

    public static class TestResult {

        public static String POSITIVE = "P - Positive";
        public static String NEGATIVE = "N - Negative";
        public static String INDETERMINATE = "I - Indeterminate";
    }

    public static class GeneralTestResult {

        public static String POSITIVE = "Positive";
        public static String NEGATIVE = "Negative";
    }

    public static class RapidTestResult {

        public static String POSITIVE = "Positive";
        public static String NEGATIVE = "Negative";
    }

    public static class HivTestResult {

        public static String POSITIVE = "Positive";
        public static String NEGATIVE = "Negative";
        public static String PREVIOUS_KNOWN = "Previously Known";
    }

    public static class TimeDiagnosis {

        public static String NEW_ANC = "Newly Tested HIV+ (ANC)";
        public static String PREVIOUS_ANC = "Previously known HIV+ (ANC)";
        public static String NEW_LD = "Newly Tested HIV+ (L&D)";
        public static String PREVIOUS_LD = "Previously known HIV+ (L&D)";
        public static String NEW_PP_LESS = "Newly Tested HIV+ (PP <=72hrs)";
        public static String PREVIOUS_PP_LESS = "Previously known HIV+ (PP <=72hrs)";
        public static String NEW_PP_GREATER = "Newly Tested HIV+ (PP>72hrs)";
        public static String PREVIOUS_PP_GREATER = "Previously known HIV+ (PP>72hrs)";
    }

    public static class YesNoOption {

        public static String YES = "Yes";
        public static String NO = "No";
    }

    public static class MaternalStatus {

        public static String PREGNANT = "Pregnant";
        public static String LABOUR = "Labour&Delivery";
        public static String PP_BREASTFEEDING = "Post Partum (Breastfeeding)";
        public static String PP_NOT_BREASTFEEDING = "Post Partum (Not breastfeeding)";
    }

    public static class PMTCTEntryPoint{
        public static String ANC = "ANC";
        public static String LABOUR = "LABOUR";
        public static String PP = "PP";
    }

    public static class Reports {
        public static Integer LTFU_PEPFAR = 28;
        public static Integer LTFU_GON = 90;
    }

    public static class GestationalPeriod {

        public static String LT_36WKS = "<= 36weeks";
        public static String GT_36WKS = "> 36weeks";
    }

    public static class BookingStatus {

        public static Integer BOOKED = 1;
        public static Integer UNBOOKED = 0;
    }

    public static class ChildStatus {

        public static String ALIVE = "A - Alive";
        public static String DEAD = "D - Dead";
    }

    public static class ArvTiming {

        public static String IN_FACILITY_72HRS = "Within 72 hrs - Facility Delivery";
        public static String OUT_FACILITY_72HRS = "Within 72 hrs - Delivered Outside Facility";
        public static String IN_FACILITY_AFTER_72HRS = "After 72 hrs - Facility Delivery";
        public static String OUT_FACILITY_AFTER_72HRS = "After 72 hrs - Delivered Outside Facility";
    }

    public static class DMOCType {

        public static String MMS = "MMS";
        public static String MMD = "MMD";
        public static String CPARP = "CPARP";
        public static String CARC = "CARC";
    }

    public static class CategoryIds {

        public static String CATEGORY_IDS_FEMALE = "1,2,3,4,5,6,7,8,9,10,11,12";
        public static String CATEGORY_IDS_MALE = "13,14,15,16,17,18,19,20,21,22,23,24";
    }

    public static class StateIds {

        public static String STATE_IDS = "2,3,4,5,6,8,9,12,18,20,25,36";
    }

    @Component
    public static class ArchiveStatus {
        public static final int UN_ARCHIVED = 0;
        public static final int ARCHIVED = 1;
        public static final int DEACTIVATED = 2;
    }
}
