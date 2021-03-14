package vp.metagram.utils.instagram.types;

import java.util.Random;

public class Feeder
{

    volatile Random random = new Random();

    volatile double[] S = new double[5];
    volatile double[][] V = new double[3][3];

    static public synchronized Feeder getNewInstance()
    {
        Feeder result = new Feeder();
        result.fill();
        return  result;
    }


    public void setV1(double x, double y, double z)
    {
        V[0][0] = x;V[0][1] = y;V[0][2] = z;
    }

    public void setV2(double x, double y, double z)
    {
        V[1][0] = x;V[1][1] = y;V[1][2] = z;
    }

    public void setV3(double x, double y, double z)
    {
        V[2][0] = x;V[2][1] = y;V[2][2] = z;
    }

    public double[] getS()
    {
        return S;
    }

    void fill()
    {
        for (int i=0; i < S.length; i++)
        { S[i] = getNextDouble(); }

        for (int i=0; i<V.length; i++)
        {
            for (int j=0; j<V[i].length; j++)
            {
                V[i][j] = getNextDouble();
            }
        }

    }

    double getNextDouble()
    {
        double result = 1 + random.nextDouble()*Math.pow(10,6);
        if (!random.nextBoolean())
        {
            result = -result;
        }
        return result;
    }

    public synchronized int getNext()
    {
        int index = random.nextInt(3);
        int result = (int)(S[index+0]*V[index][0]+ 0.01) +
                (int)(S[index+1]*V[index][1]- 0.01) + (int)(S[index+2]*V[index][2]+ 0.01) ;

        return result;
    }
}
