/*
 Complex -- a class within the Cellular Automaton Explorer. 
 Copyright (C) 2006  David B. Bahr (http://academic.regis.edu/dbahr/)

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

import java.math.BigDecimal;
import java.text.DecimalFormat;

import cellularAutomata.cellState.model.CellState;
import cellularAutomata.cellState.model.ComplexState;
import cellularAutomata.error.exceptions.ConversionException;

/**
 * Creates a complex number and implements standard complex arithmetic.
 * 
 * @author David Bahr
 */
public class Complex extends Object
{
	/**
	 * The real part of the complex number, available for easy access.
	 */
	public double real;

	/**
	 * The imaginary part of the complex number, available for easy access.
	 */
	public double imaginary;

	// warning message
	private final static String CONVERSION_ERROR = "Could not convert the string to a complex number.";

	/**
	 * Constructs the complex number from another complex number.
	 * 
	 * @param z
	 *            The complex number from which this number will be built.
	 */
	public Complex(Complex z)
	{
		this.real = z.real;
		this.imaginary = z.imaginary;
	}

	/**
	 * Constructs the complex number z = real + i*imaginary.
	 * 
	 * @param real
	 *            The real part
	 * @param imaginary
	 *            The imaginary part
	 */
	public Complex(double real, double imaginary)
	{
		this.real = real;
		this.imaginary = imaginary;
	}

	/**
	 * Constructs a complex number from a string representation. Throws a
	 * ConversionException if the string cannot be converted to a complex
	 * number.
	 * 
	 * @param z
	 *            The complex number written as a string, "u + wi", "u - wi",
	 *            "-u + wi", "-u - wi", "u", "-u", "wi",or "-wi". Note that the
	 *            spaces must be exactly as indicated. This spacing is the same
	 *            as that produced by the toString() method of this class. The
	 *            values of u and w may contain scientific notation such as
	 *            0.37E-5 which must not have any additional spaces (as shown).
	 */
	public Complex(String z) throws ConversionException
	{
		// get rid of leading and trailing white spaces
		z = z.trim();

		// the real and imaginary parts
		String realPart = null;
		String imaginaryPart = null;

		// parse at the spaces
		if(z.indexOf(" ") != -1)
		{
			// get the real part of the complex number
			realPart = z.substring(0, z.indexOf(" "));

			// get the sign of the imaginary part of the complex number
			char signOfImaginaryPart = z.charAt(z.indexOf(" ") + 1);

			// get the imaginary part, but without the trailing "i" and
			// *without* the preceding sign ("+" or "-").
			imaginaryPart = z.substring(z.lastIndexOf(" ") + 1, z.length() - 1);

			// if imaginary part is negative, include the "-" sign
			if(signOfImaginaryPart == '-')
			{
				imaginaryPart = "-" + imaginaryPart;
			}
		}
		else
		{
			// the string has only a real or only an imaginary component
			if(z.indexOf("i") != -1)
			{
				// Only imaginary. So get the imaginary part, but without the
				// trailing "i".
				imaginaryPart = z.substring(z.length() - 1);

				realPart = "0.0";
			}
			else
			{
				// Only Real.
				realPart = z;
				imaginaryPart = "0.0";
			}
		}

		// The resulting pieces may contain scientific notation such as 3.26E-4,
		// so I use BigDecimal to parse these. BigDecimal understands that
		// notation.
		if(realPart != null && imaginaryPart != null)
		{
			try
			{
				BigDecimal bigReal = new BigDecimal(realPart);
				this.real = bigReal.doubleValue();

				BigDecimal bigImaginary = new BigDecimal(imaginaryPart);
				this.imaginary = bigImaginary.doubleValue();
			}
			catch(Exception e)
			{
				// couldn't convert to doubles, so rethrow
				throw new ConversionException(CONVERSION_ERROR);
			}
		}
		else
		{
			throw new ConversionException(CONVERSION_ERROR);
		}
	}

	// Real cosh function (used to compute complex trig functions)
	private static double cosh(double theta)
	{
		return (Math.exp(theta) + Math.exp(-theta)) / 2;
	}

	// Real sinh function (used to compute complex trig functions)
	private static double sinh(double theta)
	{
		return (Math.exp(theta) - Math.exp(-theta)) / 2;
	}

	/**
	 * Adds two complex numbers. (x+i*y) + (s+i*t) = (x+s)+i*(y+t).
	 * 
	 * @param z
	 *            The first complex number to add.
	 * @param w
	 *            The second complex number to add.
	 */
	public static Complex add(Complex z, Complex w)
	{
		return plus(z, w);
	}

	/**
	 * The complex conjugate of the complex number (the conjugate of x+i*y is
	 * x-i*y).
	 * 
	 * @param z
	 *            The complex number.
	 * @return The complex conjugate of z. In other words, z-bar.
	 */
	public static Complex complexConjugate(Complex z)
	{
		return new Complex(z.getReal(), -z.getImaginary());
	}

	/**
	 * The cosine of the specified complex number. cos(z) =
	 * (exp(i*z)+exp(-i*z))/ 2.
	 * 
	 * @param z
	 *            The complex number.
	 * @return cos(z).
	 */
	public static Complex cos(Complex z)
	{
		double real = z.getReal();
		double imaginary = z.getImaginary();

		return new Complex(cosh(imaginary) * Math.cos(real), -sinh(imaginary)
				* Math.sin(real));
	}

	/**
	 * The hyperbolic cosine of the specified complex number. cosh(z) = (exp(z) +
	 * exp(-z)) / 2.
	 * 
	 * @param z
	 *            The complex number.
	 * @return cosh(z).
	 */
	public static Complex cosh(Complex z)
	{
		double real = z.getReal();
		double imaginary = z.getImaginary();

		return new Complex(cosh(real) * Math.cos(imaginary), sinh(real)
				* Math.sin(imaginary));
	}

	/**
	 * Divides one complex number by another complex number. In other words,
	 * (x+i*y)/(s+i*t) = ((x*s+y*t) + i*(y*s-y*t)) / (s^2+t^2)
	 * 
	 * @param num
	 *            The numerator.
	 * @param den
	 *            The denominator.
	 */
	public static Complex divide(Complex num, Complex den)
	{
		Complex div = new Complex(0, 0);

		double denominator = den.modulus() * den.modulus();
		div.real = (num.real * den.getReal() + num.imaginary
				* den.getImaginary())
				/ denominator;
		div.imaginary = (num.imaginary * den.getReal() - num.real
				* den.getImaginary())
				/ denominator;

		return div;
	}

	/**
	 * The complex exponential, exp(z).
	 * 
	 * @param z
	 *            The complex number.
	 * @return exp(z) where z is the complex number.
	 */
	public static Complex exp(Complex z)
	{
		double real = z.getReal();
		double imaginary = z.getImaginary();

		return new Complex(Math.exp(real) * Math.cos(imaginary), Math.exp(real)
				* Math.sin(imaginary));
	}

	/**
	 * Finds the principal branch of the complex logarithm of the specified
	 * complex number. The principal branch is the branch with -pi < arg(z) <=
	 * pi.
	 * 
	 * @param z
	 *            The complex number.
	 * @return log(z) where z is the Complex number.
	 */
	public static Complex log(Complex z)
	{
		return new Complex(Math.log(z.modulus()), z.arg());
	}

	/**
	 * Subtracts two complex numbers. (x+i*y) - (s+i*t) = (x-s)+i*(y-t).
	 * 
	 * @param z
	 *            The first complex number.
	 * @param w
	 *            The second complex number which is subtracted from the first
	 *            complex number.
	 */
	public static Complex minus(Complex z, Complex w)
	{
		Complex difference = new Complex(0, 0);
		difference.real = z.real - w.getReal();
		difference.imaginary = z.imaginary - w.getImaginary();

		return difference;
	}

	/**
	 * Mutiplies two complex numbers. In other words, (x+i*y)*(s+i*t) =
	 * ((x*s-y*t), i*(x*t-y*s))
	 * 
	 * @param z
	 *            The first number to multiply.
	 * @param w
	 *            The second number to multiply.
	 */
	public static Complex multiply(Complex z, Complex w)
	{
		return times(w, z);
	}

	/**
	 * Adds two complex numbers. (x+i*y) + (s+i*t) = (x+s)+i*(y+t).
	 * 
	 * @param z
	 *            The first complex number to add.
	 * @param w
	 *            The second complex number to add.
	 */
	public static Complex plus(Complex z, Complex w)
	{
		Complex sum = new Complex(0, 0);
		sum.real = z.real + w.getReal();
		sum.imaginary = z.imaginary + w.getImaginary();

		return sum;
	}

	/**
	 * The sine of the specified complex number. sin(z) =
	 * (exp(i*z)-exp(-i*z))/(2*i).
	 * 
	 * @param z
	 *            The complex number.
	 * @return sin(z).
	 */
	public static Complex sin(Complex z)
	{
		double real = z.getReal();
		double imaginary = z.getImaginary();

		return new Complex(cosh(imaginary) * Math.sin(real), sinh(imaginary)
				* Math.cos(real));
	}

	/**
	 * The hyperbolic sine of the specified complex number. sinh(z) =
	 * (exp(z)-exp(-z))/2.
	 * 
	 * @param z
	 *            The complex number.
	 * @return sinh(z).
	 */
	public static Complex sinh(Complex z)
	{
		double real = z.getReal();
		double imaginary = z.getImaginary();

		return new Complex(sinh(real) * Math.cos(imaginary), cosh(real)
				* Math.sin(imaginary));
	}

	/**
	 * Takes the square root of the specified complex number. Computes the
	 * principal branch of the square root, which is the value with 0 <= arg(z) <
	 * pi. In other words, computes sqrt(z).
	 * 
	 * @param z
	 *            The complex number.
	 * @return The square root of z.
	 */
	public static Complex sqrt(Complex z)
	{
		double r = Math.sqrt(z.modulus());
		double theta = z.arg() / 2;
		return new Complex(r * Math.cos(theta), r * Math.sin(theta));
	}

	/**
	 * Subtracts two complex numbers. (x+i*y) - (s+i*t) = (x-s)+i*(y-t).
	 * 
	 * @param z
	 *            The first complex number.
	 * @param w
	 *            The second complex number which is subtracted from the first
	 *            complex number.
	 */
	public static Complex subtract(Complex z, Complex w)
	{
		return minus(z, w);
	}

	/**
	 * The tangent of the specified complex number. tan(z) = sin(z)/cos(z).
	 * 
	 * @param z
	 *            The complex number.
	 * @return tan(z).
	 */
	public static Complex tan(Complex z)
	{
		return divide(sin(z), cos(z));
	}

	/**
	 * The hyperbolic tangent of the specified complex number. tanh(z) =
	 * sinh(z)/cosh(z).
	 * 
	 * @param z
	 *            The complex number.
	 * @return tanh(z).
	 */
	public static Complex tanh(Complex z)
	{
		return Complex.divide(Complex.sinh(z), Complex.cosh(z));
	}

	/**
	 * Mutiplies two complex numbers. In other words, (x+i*y)*(s+i*t) =
	 * ((x*s-y*t), i*(x*t-y*s))
	 * 
	 * @param z
	 *            The first number to multiply.
	 * @param w
	 *            The second number to multiply.
	 */
	public static Complex times(Complex z, Complex w)
	{
		Complex product = new Complex(0, 0);

		product.real = z.real * w.getReal() - z.imaginary * w.getImaginary();
		product.imaginary = z.real * w.getImaginary() + z.imaginary
				* w.getReal();

		return product;
	}

	/**
	 * The argument of the complex number (the angle from the real-axis in polar
	 * coordinates).
	 * 
	 * @return arg(z) in radians, where z is the complex number.
	 */
	public double arg()
	{
		return Math.atan2(imaginary, real);
	}

	/**
	 * Creates a clone of this complex number; this method returns a different
	 * instance of the number, but with the same value.
	 * <p>
	 * in other words, for complex number x, the expression:
	 * <code> x.clone() !=  x </code> will be true, and that the expression:
	 * <code> x.clone().getClass() == x.getClass() </code> will be true. Also:
	 * <code> x.clone().equals(x) </code> will be true.
	 * 
	 * @return A new instance of the complex number.
	 */
	public Complex clone()
	{
		// note that this clones the complex number by extracting the real and
		// imaginary components for the new complex number
		return new Complex(this);
	}

	/**
	 * Checks to see if the given complex number is equal to this complex
	 * number.
	 * 
	 * @param z
	 *            The complex number to which this one will be compared.
	 * @return true if the complex numbers are identical.
	 */
	public boolean equals(Complex z)
	{
		boolean theSame = false;

		if(this.real == z.real && this.imaginary == z.imaginary)
		{
			theSame = true;
		}

		return theSame;
	}

	/**
	 * The imaginary part of the complex number.
	 * 
	 * @return Im[z] where z is the complex number.
	 */
	public double getImaginary()
	{
		return imaginary;
	}

	/**
	 * The real part of the complex number.
	 * 
	 * @return Re[z] where z is the complex number.
	 */
	public double getReal()
	{
		return real;
	}

	/**
	 * The magnitude (or modulus) of the complex number (the distance from the
	 * origin in polar coordinates).
	 * 
	 * @return |z| where z is the complex number.
	 */
	public double magnitude()
	{
		return modulus();
	}

	/**
	 * The modulus of the complex number (the distance from the origin in polar
	 * coordinates).
	 * 
	 * @return |z| where z is the complex number.
	 */
	public double modulus()
	{
		return Math.sqrt(real * real + imaginary * imaginary);
	}

	/**
	 * Sets the imaginary part of the complex number.
	 * 
	 * @param imaginary
	 *            The imaginary part of a complex number.
	 */
	public void setImaginary(double imaginary)
	{
		this.imaginary = imaginary;
	}

	/**
	 * Sets the real part of the complex number.
	 * 
	 * @param real
	 *            The real part of a complex number.
	 */
	public void setReal(double real)
	{
		this.real = real;
	}

	/**
	 * Returns the complex number as a string, but only uses the specified
	 * number of decimal places for the real and imaginary components. In other
	 * words, truncates the output of the real and imaginary components when
	 * constructing the string.
	 * 
	 * @param numberOfDecimalPlaces
	 *            The number of decimal places (between 1 and 32 inclusive) that
	 *            will be used for the real and imaginary components of the
	 *            number.
	 * @return "x + y i", "x - y i", "x", or "y i" as appropriate, but with x
	 *         and y restricted to the specified number of decimal places.
	 */
	public String toPrettyString(int numberOfDecimalPlaces)
	{
		// The pattern used to display the components (with the specified number
		// of decimal places)
		StringBuilder builder = new StringBuilder("0.0");
		if(numberOfDecimalPlaces > 1 && numberOfDecimalPlaces <= 32)
		{
			// add zeroes to the end until have the correct number of decimal
			// places (start with 1 because already have one decimal place).
			for(int i = 1; i < numberOfDecimalPlaces; i++)
			{
				builder.append("0");
			}
		}
		String decimalPattern = builder.toString();

		// set the format for each component
		DecimalFormat numberFormatter = new DecimalFormat(decimalPattern);
		String realString = numberFormatter.format(real);
		String imaginaryString = numberFormatter.format(imaginary);
		String negativeImaginaryString = numberFormatter.format(-imaginary);

		// build the complex number as a string
		String complexNumberAsString = "";
		if(real != 0 && imaginary > 0)
		{
			complexNumberAsString = realString + " + " + imaginaryString + "i";
		}
		else if(real != 0 && imaginary < 0)
		{
			complexNumberAsString = realString + " - "
					+ negativeImaginaryString + "i";
		}
		else if(imaginary == 0)
		{
			complexNumberAsString = realString;
		}
		else if(real == 0)
		{
			complexNumberAsString = imaginaryString + "i";
		}
		else
		{
			// just to be safe
			complexNumberAsString = realString + " + " + imaginaryString + "i";
		}

		return complexNumberAsString;
	}

	/**
	 * String representation of this complex number.
	 * 
	 * @return Will return one of "u + wi", "u - wi", "-u + wi", "-u - wi", "u",
	 *         "-u", "wi",or "-wi" as appropriate. Note that the spaces will be
	 *         exactly as indicated. The values of u and w may contain
	 *         scientific notation such as 0.37E-5 which will not have any
	 *         additional spaces.
	 */
	public String toString()
	{
		String complexNumberAsString = "";
		if(real != 0 && imaginary > 0)
		{
			complexNumberAsString = real + " + " + imaginary + "i";
		}
		else if(real != 0 && imaginary < 0)
		{
			complexNumberAsString = real + " - " + (-imaginary) + "i";
		}
		else if(imaginary == 0)
		{
			complexNumberAsString = String.valueOf(real);
		}
		else if(real == 0)
		{
			complexNumberAsString = imaginary + "i";
		}
		else
		{
			// just to be safe
			complexNumberAsString = real + " + " + imaginary + "i";
		}

		return complexNumberAsString;
	}
}
