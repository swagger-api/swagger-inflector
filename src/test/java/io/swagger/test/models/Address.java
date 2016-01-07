/*
 *  Copyright 2016 SmartBear Software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.swagger.test.models;

public class Address {
    private String street;
    private String city;
    private String state;
    private String zip;

    public String getStreet() {
      return this.street;
    }
    public void setStreet(String street) {
      this.street = street;
    }

    public String getCity() {
      return this.city;
    }
    public void setCity(String city) {
      this.city = city;
    }

    public String getState() {
      return this.state;
    }
    public void setState(String state) {
      this.state = state;
    }

    public String getZip() {
      return this.zip;    
    }
    public void setZip(String zip) {
      this.zip = zip;
    }
}