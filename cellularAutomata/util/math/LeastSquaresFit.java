/*
 LeastSquaresFit -- a class within the Cellular Automaton Explorer. 
 Copyright (C) 2005  David B. Bahr (http://academic.regis.edu/dbahr/)

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package cellularAutomata.util.math;

import java.awt.geom.Point2D;
import java.util.List;

/**
 * Finds a linear least squares fit to data. The solution has the form mx+b.
 * 
 * @author David Bahr
 */
public class LeastSquaresFit
{
    // The points that will be fitted.
    private Point2D[] points = null;

    // the measure of goodness of fit, chi squared
    private double chiSquared = 0.0;

    // the linear correlation coefficient
    private double rSquared = 0.0;

    // slope of the best fit line
    private double slope = 0.0;

    // the standard deviation of the slope
    private double standardDeviationSlope = 0.0;

    // the standard deviation of the y-intercept
    private double standardDeviationYIntercept = 0.0;

    // the variance (standard deviation squared) of the estimated slope
    private double varianceInSlope = 0.0;

    // the variance (standard deviation squared) of the estimated y-intercept
    private double varianceInYIntercept = 0.0;

    // the y-intercept of the best fit line.
    private double yIntercept = 0.0;

    /**
     * Finds a least squares fit to the supplied data points.
     * 
     * @param listOfPoints
     *            The list of points that will be fitted with a least squares.
     *            The elements of the list must be objects of type
     *            Point2D.Double.
     */
    public void fit(List listOfPoints)
    {
        if(listOfPoints != null && listOfPoints.size() > 1)
        {
            Object[] list = listOfPoints.toArray();

            this.points = new Point2D.Double[list.length];
            for(int i = 0; i < list.length; i++)
            {
                this.points[i] = (Point2D) list[i];
            }
        }

        fit(points);
    }

    /**
     * Finds a least squares fit to the supplied data points. this uses the
     * equations as outlined in Numerical Recipes, but the code has been
     * specially adapted for my purposes.
     * 
     * @param points
     *            The array of points that will be fitted with a least squares.
     */
    public void fit(Point2D[] points)
    {
        if(points != null && points.length > 1)
        {
            double sumX = 0.0;
            double sumY = 0.0;
            for(int i = 0; i < points.length; i++)
            {
                sumX += points[i].getX();
                sumY += points[i].getY();
            }

            // t and sumOfTSquared are just intermediate functions
            double sumOfTSquared = 0.0;
            for(int i = 0; i < points.length; i++)
            {
                double t = points[i].getX() - (sumX / points.length);
                sumOfTSquared += t * t;
                slope += t * points[i].getY();
            }

            // find the slope
            slope /= sumOfTSquared;

            // find y-interecept
            yIntercept = (sumY - (sumX * slope)) / points.length;

            // find the standard deviation of slope and y-intercept
            standardDeviationSlope = Math.sqrt(1.0 / sumOfTSquared);
            standardDeviationYIntercept = Math
                .sqrt((1.0 + ((sumX * sumX) / (points.length * sumOfTSquared)))
                    / points.length);

            // find the linear correlation coefficient
            double covariance = -sumX / (points.length * sumOfTSquared);
            rSquared = covariance
                / (standardDeviationSlope * standardDeviationYIntercept);
            rSquared *= rSquared;

            // find chi squared
            for(int i = 0; i < points.length; i++)
            {
                double difference = (points[i].getY() - yIntercept - slope
                    * points[i].getX());
                chiSquared += difference * difference;
            }

            // adjust the standard deviation of the slope and y-intercept
            // (adjusted by chi squared)
            double adjustment = Math.sqrt(chiSquared / (points.length - 2.0));
            standardDeviationSlope *= adjustment;
            standardDeviationYIntercept *= adjustment;
        }
    }

    /**
     * Gets the goodness of fit estimate. Will be 0.0 until the fit method is
     * run.
     * 
     * @return the chiSquared
     */
    public double getChiSquared()
    {
        return chiSquared;
    }

    /**
     * Gets the linear correlation coefficient. Will be 0.0 until the fit method
     * is run.
     * 
     * @return the rSquared
     */
    public double getRSquared()
    {
        return rSquared;
    }

    /**
     * Gets the slope of the linear fit. In other words this is "m" in y = mx+b.
     * Will be 0.0 until the fit method is run.
     * 
     * @return the slope
     */
    public double getSlope()
    {
        return slope;
    }

    /**
     * Gets the standard deviation of the slope of the linear fit. Will be 0.0
     * until the fit method is run.
     * 
     * @return the standardDeviationSlope
     */
    public double getStandardDeviationSlope()
    {
        return standardDeviationSlope;
    }

    /**
     * Gets the standard deviation of the y-intercept of the linear fit. Will be
     * 0.0 until the fit method is run.
     * 
     * @return the standardDeviationYIntercept
     */
    public double getStandardDeviationYIntercept()
    {
        return standardDeviationYIntercept;
    }

    /**
     * Gets the y-intercept of the linear fit. In other words this is "b" in y =
     * mx+b. Will be 0.0 until the fit method is run.
     * 
     * @return the yIntercept
     */
    public double getYIntercept()
    {
        return yIntercept;
    }
}
