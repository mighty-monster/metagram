package vp.metagram.ui.ReportUI.chartViewer.types;

import vp.metagram.R;

import static vp.metagram.general.variables.appContext;

public enum QueryStepType
{
    perCall(101),
    perHour(102),
    perDay(103),
    perWeek(104),
    perMonth(105);

    int ordinal;

    QueryStepType(int ordinal) { this.ordinal = ordinal;}

    public int getOrdinal()
    {
        return ordinal;
    }

    public String getStepTitle()
    {
        String result = "";

        switch ( ordinal )
        {
            case 101:
                result = appContext.getResources().getString(R.string.queryStep_p2p);
                break;
            case 102:
                result = appContext.getResources().getString(R.string.queryStep_hourly);
                break;
            case 103:
                result = appContext.getResources().getString(R.string.queryStep_daily);
                break;
            case 104:
                result = appContext.getResources().getString(R.string.queryStep_weekly);
                break;
            case 105:
                result = appContext.getResources().getString(R.string.queryStep_monthly);
                break;
        }


        return result;
    }

    public static QueryStepType createFromTitle(String Title)
    {
        QueryStepType result = null;
        for ( QueryStepType stepType : QueryStepType.values() )
        {
            if ( stepType.getStepTitle().equals(Title) )
            {
                result = stepType;
                break;
            }
        }
        return result;
    }

    static QueryStepType createFromOrdinal(int ordinal)
    {
        QueryStepType result = null;
        for ( QueryStepType stepType : QueryStepType.values() )
        {
            if ( stepType.ordinal == ordinal )
            {
                result = stepType;
                break;
            }
        }
        return result;
    }
}
