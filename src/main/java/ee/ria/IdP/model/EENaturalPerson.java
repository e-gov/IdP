/*
 * MIT License
 *
 * Copyright (c) 2018 Estonian Information System Authority
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package ee.ria.IdP.model;

import ee.ria.IdP.exceptions.InvalidAuthData;
import org.joda.time.DateTime;

import java.io.Serializable;

/**
 * Model object for natural persons
 */
public class EENaturalPerson implements Serializable {
    private String familyName;
    private String firstName;
    private String idCode;

    private DateTime birthDate;
    public EENaturalPerson(String familyName, String firstName, String idCode) throws InvalidAuthData {
        this.familyName = familyName;
        this.firstName = firstName;
        this.idCode = idCode;

        this.birthDate = calcBirthDate();
    }

    public String getFamilyName() {
        return familyName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getIdCode() {
        return idCode;
    }

    public DateTime getBirthDate() {
        return birthDate;
    }

    private DateTime calcBirthDate() throws InvalidAuthData {
        if(idCode.length() != 11)
            throw new InvalidAuthData("invalid.cert");
        int century;
        switch(idCode.substring(0,1)) {
            case "1":
            case "2":
                century = 1800; break;
            case "3":
            case "4":
                century = 1900; break;
            case "5":
            case "6":
                century = 2000; break;
            default:
                throw new InvalidAuthData("invalid.cert");
        }
        try {
            int year = Integer.parseInt(idCode.substring(1, 3));
            int month = Integer.parseInt(idCode.substring(3, 5));
            int day = Integer.parseInt(idCode.substring(5, 7));
            return new DateTime().withDate(century+year,month,day);
        }
        catch (Exception e) {
            throw new InvalidAuthData("invalid.cert");
        }
    }


}
