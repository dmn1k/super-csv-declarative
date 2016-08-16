# super-csv-declarative [![Build Status](https://travis-ci.org/dkschlos/super-csv-declarative.svg?branch=master)](https://travis-ci.org/dkschlos/super-csv-declarative) [![codecov](https://codecov.io/gh/dkschlos/super-csv-declarative/branch/master/graph/badge.svg)](https://codecov.io/gh/dkschlos/super-csv-declarative)

Unofficial declarative extension to super-csv, currently supporting version 2.4.0.

It mainly provides two new classes:
- CsvDeclarativeBeanReader
- CsvDeclarativeBeanWriter

Those can be used to read/write CSV-files from/to java beans via conventions and declarative mappings:

```Java
public class Person {
	@Trim
	private String name;
	
	@Optional
	@Trim
	private String middleName;
	
	@Trim
	private String lastName;
	
	private int age;
	private double weight;

	// getters and setters omitted
}
```

This example class leads to an implicit mapping, where csv-file-cells are mapped to properties via implicit order in the class.
CellProcessor-Pipelines are created by annotations or, if absent and applicable, by conventions.
This means, that the above annotated fields lead to a CellProcessor-map like this:

```Java
CellProcessor[] processors = new CellProcessor[] {
			new Trim(),
			new Optional(new Trim()),
			new Trim(),
			new ParseInt(),
			new ParseDouble()	
		};
```

## Field access strategies

By default, super-csv-declarative uses getters and setters to access bean-fields.
To force super-csv-declarative to bypass getters and setters and use the fields directly (for example to build immutable classes), one can annotate a bean with *@CsvAccessorType*:

```Java
@CsvAccessorType(CsvAccessType.FIELD)
public class MyBean {
	// content omitted
}
```

## Ignoring fields

```Java
public class Person {
	private static final int THE_ANSWER = 42;
	
	@CsvTransient
	private String toIgnore;
}
```

Static fields are ignored as well as all fields annotated with *@CsvTransient*.

## Explicit field/annotation-order

**Note**: The Java Language Specification doesn't specify the order in which fields of a class or annotations are returned when using reflection. The Oracle JVM does return them in the declared order but others like Dalvik may sort them alphabetically or in any other way.

If your application needs to support such environments you should use the *@CsvField*-annotation for fields and the *index*-fields which is defined in all standard CellProcessor-annotations and can be added to custom ones as well:

```Java
public class Person {
	@Trim
	@CsvField(index = 0)
	private String name;
	
	@Optional(index = 0)
	@Trim(index = 1)
	@CsvField(index = 1)
	private String middleName;
	
	@Trim
	@CsvField(index = 2)
	private String lastName;
	
	@CsvField(index = 3)
	private int age;
	
	@CsvField(index = 4)
	private double weight;

	// getters omitted
}
```

## Mapping modes

The default mapping mode is *STRICT* which means that you have to use *@CsvField* on all fields or on no field at all.
It also means that you have to have a bean-field for each field in the CSV-file when reading.

There is another mapping mode, *LOOSE*, which allows you to partially map fields in a bean by using *@CsvField* only on some fields.
It also allows you to ignore fields in a source CSV-file.

You can change the mapping mode by applying the *@CsvMappingMode*-annotation to beans:

```Java
@CsvMappingMode(CsvMappingModeType.LOOSE)
public class MyBean {
	// content omitted
}
```


## Implementing new Processors

If you want to add a new processor and use it in a declarative way, you need to implement the corresponding *annotation* and a *DeclarativeCellProcessorProvider*-implementation which gets the annotation-instance and creates a *CellProcessorFactory*.

The following example shows how to implement all those necessary parts:

### The annotation

```Java
@CellProcessorAnnotationDescriptor(provider = OptionalCellProcessorProvider.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface Optional {
	int index() default ProcessorOrder.UNDEFINED;
}
```

### The provider

```Java
public class OptionalCellProcessorProvider implements
	DeclarativeCellProcessorProvider<org.supercsv.io.declarative.annotation.Optional> {
	
	public CellProcessorFactory create(final org.supercsv.io.declarative.annotation.Optional annotation) {
		return new CellProcessorFactory() {
			
			public int getIndex() {
				return annotation.index();
			}
			
			public CellProcessor create(CellProcessor next) {
				return new Optional(next);
			}
		};
	}
	
	public Class<org.supercsv.io.declarative.annotation.Optional> getType() {
		return org.supercsv.io.declarative.annotation.Optional.class;
	}
	
}
```

## Installation

Get it from Maven Central:
```Maven
<dependency>
    <groupId>com.github.dkschlos</groupId>
    <artifactId>super-csv-declarative</artifactId>
    <version>2.0.0</version>
</dependency>
```
