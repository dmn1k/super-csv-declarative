/*
 * Copyright 2016 dominik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.dkschlos.supercsv.testbeans;

import com.github.dkschlos.supercsv.io.declarative.annotation.CellProcessorFactoryMethod;
import com.github.dkschlos.supercsv.io.declarative.annotation.CsvAccessType;
import com.github.dkschlos.supercsv.io.declarative.annotation.CsvAccessorType;
import com.github.dkschlos.supercsv.io.declarative.annotation.Optional;
import com.github.dkschlos.supercsv.io.declarative.annotation.StrReplace;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.cellprocessor.ift.StringCellProcessor;

@CsvAccessorType(CsvAccessType.FIELD)
public class BeanWithCellProcessorFactoryMethod {

    @Optional
    private String name;
    
    @StrReplace(pattern = "^", replacement = " ")
    @StrReplace(pattern = "a", replacement = "b")
    @CellProcessorFactoryMethod(methodName = "createProcessor")
    private String lastName;
    private int age;
    private double weight;

    public static CellProcessor createProcessor(CellProcessor next){
        return new org.supercsv.cellprocessor.Trim(new org.supercsv.cellprocessor.StrReplace("b", "c", (StringCellProcessor) next));
    }
    
    public BeanWithCellProcessorFactoryMethod() {
    }

    public BeanWithCellProcessorFactoryMethod(String name, String lastName, int age, double weight) {
        this.name = name;
        this.lastName = lastName;
        this.age = age;
        this.weight = weight;
    }

    public String getName() {
        return name;
    }

    public String getLastName() {
        return lastName;
    }

    public int getAge() {
        return age;
    }

    public double getWeight() {
        return weight;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + age;
        result = prime * result + ((lastName == null) ? 0 : lastName.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        long temp;
        temp = Double.doubleToLongBits(weight);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        BeanWithCellProcessorFactoryMethod other = (BeanWithCellProcessorFactoryMethod) obj;
        if (age != other.age) {
            return false;
        }
        if (lastName == null) {
            if (other.lastName != null) {
                return false;
            }
        } else if (!lastName.equals(other.lastName)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return Double.doubleToLongBits(weight) == Double.doubleToLongBits(other.weight);
    }

    @Override
    public String toString() {
        return "BeanWithRepeatableAnnotation [name=" + name + ", lastName=" + lastName + ", age=" + age
                + ", weight=" + weight + "]";
    }

}