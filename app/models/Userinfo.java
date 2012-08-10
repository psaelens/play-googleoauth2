/*
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
/*
 * Warning! This file is generated. Modify at your own risk.
 */

package models;




/**
 * Model definition for Userinfo.
 *
 * <p> This is the Java data model class that specifies how to parse/serialize into the JSON that is
 * transmitted over HTTP when working with the . For a detailed explanation see:
 * <a href="http://code.google.com/p/google-api-java-client/wiki/Json">http://code.google.com/p/google-api-java-client/wiki/Json</a>
 * </p>
 *
 * @author Google, Inc.
 */
public final class Userinfo {


  /**
   * The user's last name.
   * The value may be {@code null}.
   */
  public String family_name;



  /**
   * The user's full name.
   * The value may be {@code null}.
   */
  public String name;



  /**
   * URL of the user's picture image.
   * The value may be {@code null}.
   */
  public String picture;



  /**
   * The user's default locale.
   * The value may be {@code null}.
   */
  public String locale;



  /**
   * The user's gender.
   * The value may be {@code null}.
   */
  public String gender;



  /**
   * The user's email address.
   * The value may be {@code null}.
   */
  public String email;



  /**
   * The user's birthday. The year is not present.
   * The value may be {@code null}.
   */
  public String birthday;



  /**
   * URL of the profile page.
   * The value may be {@code null}.
   */
  public String link;



  /**
   * The user's first name.
   * The value may be {@code null}.
   */
  public String given_name;



  /**
   * The user's default timezone.
   * The value may be {@code null}.
   */
  public String timezone;



  /**
   * The focus obfuscated gaia id of the user.
   * The value may be {@code null}.
   */
  public String id;



  /**
   * Boolean flag which is true if the email address is verified.
   * The value may be {@code null}.
   */
	public Boolean verified_email;

}