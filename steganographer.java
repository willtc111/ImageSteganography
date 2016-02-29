/**
 * Steganography Program
 * By William Carver
 * 
 * Known Bugs:
 *  - will decode an addition an additional character at the end of the message when using certain pics.
 *    I think this is caused by the dimensions of the pic causing the message to not fit perfectly leaving
 *    an extra un-encoded pixel during the encoding process
 *  - Certain images will decode with unusual character replacements such as n instead of t, etc.  This
 *    only happens with certain images and some messages become more messed up than others.  This problem has been replicated
 *    with a pine tree picture, with t being replaced by n in the decoded message in multiple tests.  tree.jpg = 420x540
 *    
 *  - 
 *    
 */

import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import java.util.*;

public class steganographer {

	public static void main(String[] args) throws IOException {
		while( true ) {
			BufferedImage Img = null;
			int[] RGBvalues = null;
			File image = null;
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			System.out.println( "\nDo you want to:\n1) Encode\n2) Decode\n3) Exit");
			String input = br.readLine();
//			System.out.println( "[] Input: " + input );  //DEBUG
			if( input.compareTo( "3" ) == 0 )	{
				System.out.println( "Exiting..." );
				System.exit(0);
			} else if( input.compareTo( "2" ) == 0 ) {
				
//				System.out.println( "Yo Dawg, I heard you like decoding!");  //DEBUG
				
				boolean working = false;
				while( !working ) {
					try {
						System.out.println( "\nEnter the name of the file to be decoded: " );
						String fileName = br.readLine();
						image = new File( "C:\\Users\\William\\Pictures\\" + fileName );
						Img = ImageIO.read( image );
						working = true;
					}
					catch(IOException e) {
						//e.printStackTrace();
						System.out.println( "*** Something isn't right, try again." );
					};
				}
				int width = Img.getWidth();
				int height = Img.getHeight();
				RGBvalues = Img.getRGB( 0, 0, width, height, null, 0, width );
				int spacing = readSpacing( RGBvalues[0] );
//				System.out.println( "Read Spacing: " + spacing);  //DEBUG
				String message = "";
				for( int n = spacing; n < RGBvalues.length; n = n + spacing ) {
					System.out.println( ">>> Reading pixel " + n );
					message = message + (char)getChar( RGBvalues[n] );
				}
				System.out.println( "The message is:\n" + message );
				
			}else if( input.compareTo( "1" ) == 0 ) {
//				System.out.println( "Yo Dawg, I heard you like encoding!");  //DEBUG
				String message = null;
				String originalFileName = null;
				String newFileName = null;
				boolean worked = false;
				while( !worked ) {
					try {
						System.out.println( "\nEnter the super secret message: " );
						message = br.readLine();
						System.out.println( "Enter the name of the file to be encoded: " );
						originalFileName = br.readLine();
						System.out.println( "Enter the name of the new image file: " );
						newFileName = "m" + br.readLine() + ".png";
						image = new File( "C:\\Users\\William\\Pictures\\" + originalFileName );
//						System.out.println( "File found, reading image..." ); //DEBUG
						Img = ImageIO.read( image );
						worked = true;
					} catch (IOException e1) {
						//e1.printStackTrace();
						System.out.println( "*** Something isn't right, try again." );
					}
				}
				
				char[] mArray = message.toCharArray();
				int width = Img.getWidth();
				int height = Img.getHeight();
				
				RGBvalues = Img.getRGB( 0, 0, width, height, null, 0, width );
				int spacing = getSpacing( width, RGBvalues.length, mArray.length );
					
				//set first pixel to the number of spaces in between each important pixel
				RGBvalues[0] = setSpacingPixel( RGBvalues[0], spacing );
				
				//iterate through image and set the least important values of each RGB to the hundreds, tens, and ones respectively.
				int index;
				for( int n = 0; n < mArray.length; n++ ) {
					index = ( n + 1 ) * spacing;
					System.out.println( "[] encoding pixel " + index + " with value " + mArray[n] + " of message." );  //DEBUG
					RGBvalues[index] = encodePixel( RGBvalues[index], mArray[n] );
				}
				
				//set the RGBvalues array into an actual image
				BufferedImage mImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
				int x;
				int y;
				for( int n = 0; n < RGBvalues.length; n++ ) {
					x = (n%width);
					y = (int) n/width;
//					System.out.println( "setting new image value at x: " + (x+1) + " y: " + (y+1) );  //DEBUG
					mImg.setRGB(x, y, RGBvalues[n]);
				}

				System.out.print( "Spacing pixel of new image:      " );  printPixelARGB(mImg.getRGB(0, 0));  //DEBUG
				System.out.println( "First encoded pixel with encoded value of " + (int) mArray[0] + " is:");  //DEBUG
				printPixelARGB(mImg.getRGB(spacing%mImg.getWidth(), (int)spacing/mImg.getWidth()));  //DEBUG
				System.out.println( "Second encoded pixel with encoded value of " + (int) mArray[1] + " is:");  //DEBUG
				printPixelARGB(mImg.getRGB((2*spacing)%mImg.getWidth(), (int)(2*spacing)/mImg.getWidth()));  //DEBUG
						
				System.out.println( "Saving Image as \"" + newFileName + "\"" );  //DEBUG
				File saveMe = new File( "C:\\Users\\William\\Pictures\\" + newFileName );
				try {
					ImageIO.write( mImg, "PNG", saveMe );
					
				} catch (IOException e) {
					//e.printStackTrace();
					System.err.println( "FAILED TO WRITE IMAGE" );
				}
			}
		}
	}

	public static char getChar( int pixel ) {
		int a = (pixel >> 24) & 0xff;
		int r = (pixel >> 16) & 0xff;
		int g = (pixel >> 8) & 0xff;
		int b = (pixel) & 0xff;
		int ones = b%10;
		int tens = g%10;
		int hundreds = r%10;
		int result = (hundreds*100) + (tens*10) + ones;
		System.out.println(">>> read argb for [" + (char)result + "]: " + a + ", " + r + ", " + g + ", " + b);  //DEBUG
		return (char)result;
	}
	
	public static int readSpacing( int pixel ) {
		int a = (pixel >> 24) & 0xff;
		int r = (pixel >> 16) & 0xff;
		int g = (pixel >> 8) & 0xff;
		int b = (pixel) & 0xff;
		int tens = b%100;
		int thousands = g%100;
		int hThousands = r%100;
		int result = (hThousands*10000) + (thousands*100) + tens;
//		System.out.println( ">>> Read spacing is " + result );  //DEBUG
		return result;		
	}
	
	public static int setSpacingPixel( int pixel, int spacing) {
//		System.out.print("[] before spacing info encoding  ");  printPixelARGB(pixel);  //DEBUG
		int a = (pixel >> 24) & 0xff;
		int r = (pixel >> 16) & 0xff;
		int g = (pixel >> 8) & 0xff;
		int b = (pixel) & 0xff;
		int tens = spacing%100;
		int thousands = (spacing%10000-tens)/100;
		int hThousands = (spacing-thousands)/10000;
		if( r > 100 ) { r = 100 + hThousands;	}else { r = hThousands; }
		if( g > 100 ) { g = 100 + thousands;	}else { g = thousands; }
		if( b > 100 ) { b = 100 + tens;	}else { b = tens; }
		pixel = (a << 24) | (r << 16) | (g << 8) | b;
//		System.out.print("[] after spacing info encoding   ");  printPixelARGB(pixel);  //DEBUG
		return pixel;
	}
	
	public static int getSpacing( int width, int totalLength, int messageLength) {
//		System.out.println( "]]] Getting Spacing:  width = " + width );  //DEBUG
		int mult = 1;
		for( int n = 1; ; n++ )	{
//			System.out.println( "Trying " + n + ".  Remainder = " + ( width % n ) );
			if((width % n != 0) && (width % n != 1)) 	{
				mult = n;
				break;
			}
		}
//		System.out.println( "[] spacing multiplier: ... " + mult );  //DEBUG		
		int result = (totalLength / (messageLength+1)) - ((totalLength / (messageLength+1)) % mult);
//		System.out.println( "[] best spacing: ......... " + result );  //DEBUG
		if ( result < mult ) {
			System.out.println( "Please use a shorter message or larger image and try again" );
			System.exit( 0 );
		}
		return result;
	}
	
	
	public static void printPixelARGB(int pixel) {
		int a = (pixel >> 24) & 0xff;
		int r = (pixel >> 16) & 0xff;
		int g = (pixel >> 8) & 0xff;
		int b = (pixel) & 0xff;
		System.out.println("argb: " + a + ", " + r + ", " + g + ", " + b);
	}
	
	public static int encodePixel(int pixel, int value)	{
		if( value > 999 ) {	System.err.println( "BAD VALUE SIZE" ); System.exit(0);	}  //ERROR CHECKING AND DEBUG
		int a = (pixel >> 24) & 0xff;
		int r = (pixel >> 16) & 0xff;
		int g = (pixel >> 8) & 0xff;
		int b = (pixel) & 0xff;
		System.out.println( "[] pre %10:                  argb: " + a + ", " + r + ", " + g + ", " + b );  //DEBUG
		//remove the ones digit from the RGB values
		r = r-(r%10);
		g = g-(g%10);
		b = b-(b%10);
		System.out.println( "[] post %10:                 argb: " + a + ", " + r + ", " + g + ", " + b );  //DEBUG
		//get the one, ten, and hundred digits from the value
		int ones = (value%10);
		int tens = (value - ones)%100;
		int hundreds = (value - (tens + ones))%1000;
		//add the value's digits to the ones place in the RGB
		r = r + hundreds/100;
		g = g + tens/10;
		b = b + ones;
		System.out.println( "[] encoded value: " + value + "(" + (char)value+ ")" + ", pixel argb: " + a + ", " + r + ", " + g + ", " + b );  //DEBUG
		//recombine the ARGB into one pixel
		pixel = (a << 24) | (r << 16) | (g << 8) | b;
		return pixel;
	}
	
	
}
