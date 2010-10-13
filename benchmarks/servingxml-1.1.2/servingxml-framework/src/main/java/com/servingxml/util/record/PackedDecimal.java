/**
 *  ServingXML
 *  
 *  Copyright (C) 2007  Daniel Parker
 *    daniel.parker@servingxml.com 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 **/

package com.servingxml.util.record;

import com.servingxml.util.HexBin;
import com.servingxml.util.ByteHelper;
import com.servingxml.util.ServingXmlException;

public class PackedDecimal {

  private static final byte SIGNED_NEGATIVE = 0x0D; 
  private static final byte SIGNED_NEGATIVE2 = 0x0B; 
  private static final byte SIGNED_POSITIVE = 0x0C; 
  private static final byte SIGNED_POSITIVE2 = 0x0A; 
  private static final byte SIGNED_POSITIVE3 = 0x0E; 
  private static final byte UNSIGNED = 0x0F;

  private static final int INITIAL_STRING_SIZE = 100; 
  private static final int MAX_DECIMAL_SIZE = 20;

  protected byte[] data; 
  private int digitCount;
  private int decimalPlaces;

  /**
  * Constructs a newly allocated <code>PackedDecimal</code> object
  * from a byte array. 
  *
  * @param data byte array containing a COBOL packed decimal number
  * @param digitCount the total number of digits in the packed decimal number
  * @param decimalPlaces the number of places to the right of the implied decimal
  * 
  */
  public PackedDecimal(byte[] data, int digitCount, int decimalPlaces) {
    this.data = data;
    this.digitCount = digitCount;
    this.decimalPlaces = decimalPlaces;
  }

  public int digitCount() {
    return digitCount;
  }

  public int decimalPlaces() {
    return decimalPlaces;
  }

  public byte[] getPackedData() {
    return data;
  }

  public static int calculatePackedSize(int digitCount) {
    int size = (digitCount % 2 == 0) ? (digitCount / 2) + 1 : (digitCount + 1) / 2;
    return size;
  }


  /**
  * Returns a new <code>PackedDecimal</code> initialized to the value represented by the specified 
  * <code>String</code>
  * @param s the string to be parsed
  * @param digitCount the total number of digits in the packed decimal number
  * @param decimalPlaces the number of places to the right of the implied decimal
  */
  public static PackedDecimal parse(String s, int digitCount, int decimalPlaces)
  throws NumberFormatException {

    int wholeDigitCount = 0;
    int decimalDigitCount = 0; 

    boolean hasDecimalPart = false; 
    int decimalPos = 0;

    char[] decimalDigitBuf = new char[MAX_DECIMAL_SIZE];
    char[] wholeDigitBuf = new char[MAX_DECIMAL_SIZE]; 
    boolean negative = false; 

    int length = s.length(); 
    for (int index = 0; index < length; ++index) {
      char ch = s.charAt(index); 

      switch (ch) {
        case '+':
          if (index != 0)
            throw new NumberFormatException("Unexpected character " + ch);
          break;

        case '-':
          if (index != 0)
            throw new NumberFormatException("Unexpected character " + ch);
          else
            negative = true;
          break;
        case ',':
        case ' ':
        case '$':
          break;

        case '.':
          hasDecimalPart = true;
          decimalPos = 0;
          break;

        case '0':
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
          if (hasDecimalPart) {
            decimalDigitBuf[decimalPos++] = ch;
            decimalDigitCount++;
          } else {
            wholeDigitBuf[wholeDigitCount] = ch;
            wholeDigitCount++;
          }
          break;

        default:
          throw new NumberFormatException(
            "Unexpected character " + ch);
      }
    }

    int size = calculatePackedSize(digitCount);

    boolean Decimal_Part = false; 
    byte digit; 
    boolean highNibble = true; 
    int CurIntSize = 0;  

    int wholeIndex = wholeDigitCount; 
    int decimalIndex = decimalPlaces; 

    byte[] data = new byte[size]; 

    int i = size - 1;

    if (negative)
      data[i] = SIGNED_NEGATIVE;
    else
      data[i] = SIGNED_POSITIVE;

    if (decimalPlaces != 0)
      Decimal_Part = true;

    while ((i != -1) && wholeIndex != 0) {
      if (Decimal_Part) {
        if (decimalIndex > decimalDigitCount) {
          --decimalIndex;
          digit = 0;
        } else {
          --decimalIndex;
          char ch = decimalDigitBuf[decimalIndex];
          digit = ByteHelper.makeByte(ByteHelper.lowNibble((byte)Character.getNumericValue(ch)),
        (byte)(0));
        }
        if (decimalIndex == 0) {
          Decimal_Part = false;
        }
      } else {
        --wholeIndex;
        char ch = wholeDigitBuf[wholeIndex];
        digit = ByteHelper.makeByte(
          ByteHelper.lowNibble((byte)Character.getNumericValue(ch)),
          ByteHelper.lowNibble((byte)(0)));  
      }

      if (highNibble) {
        data[i] = ByteHelper.makeByte(ByteHelper.lowNibble(data[i]),
                         ByteHelper.lowNibble(digit));
        i--;
      } else {
        data[i] = ByteHelper.makeByte(ByteHelper.lowNibble(digit),
                         ByteHelper.lowNibble(data[i]));
      }

      highNibble = !highNibble; 
    }

    return new PackedDecimal(data, digitCount, decimalPlaces);
  }

  /**
  * Returns a string representation of this packed decimal object
  *
  * @return a string representation of this packed decimal object
  */
  public String toString() {
    boolean leadingZero = true; 
    boolean negative = false;

    int wholeDigits = digitCount - decimalPlaces;

    boolean zeroDotDecimal = (decimalPlaces != 0) ? true : false;

    StringBuilder buffer = new StringBuilder(INITIAL_STRING_SIZE);

    boolean packEven = ((wholeDigits + decimalPlaces) % 2) == 0;
    boolean highNibble = packEven ? false : true; 

    boolean done = false;
    for (int i = 0, byteIndex = 0; byteIndex < data.length && !done; ++i) {

      byte digit;
      if (highNibble) {
        digit = ByteHelper.highNibble(data[byteIndex]);
      } else {
        digit = ByteHelper.lowNibble(data[byteIndex]);
        byteIndex++;
      }

      switch (digit) {
        case SIGNED_POSITIVE:
        case SIGNED_POSITIVE2:
        case SIGNED_POSITIVE3:
        case UNSIGNED:
          done = true;
          break;

        case SIGNED_NEGATIVE:
        case SIGNED_NEGATIVE2:
          done = true;
          negative = true;
          break;

        case 0:
        case 1:
        case 2:
        case 3:
        case 4:
        case 5:
        case 6:
        case 7:
        case 8:
        case 9:
          if (digit != 0 || !leadingZero) {
            leadingZero = false; 
            buffer.append(HexBin.lookUpHexAlphabet[digit]);
            if (i == (wholeDigits-1) && decimalPlaces != 0) {
              buffer.append(".");
              zeroDotDecimal = false;
            }
          }
          break;

        default:
          throw new ServingXmlException("Unrecognized digit");
      } 
      highNibble = !highNibble;
    } 

    String s;
    if (leadingZero) {
      s = "0";
    } else if (zeroDotDecimal) {
      if (negative) {
        buffer.insert(0, "-0.");
      } else {
        buffer.insert(0, "0.");
      }
      s = buffer.toString();
    } else {
      if (wholeDigits == 0) {
        buffer.append("0.");
      }
      if (negative) {
        buffer.insert(0,"-");
      }
      s = buffer.toString();
    }
    return s;
  }
}

