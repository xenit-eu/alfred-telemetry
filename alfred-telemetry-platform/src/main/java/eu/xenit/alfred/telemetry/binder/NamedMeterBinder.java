package eu.xenit.alfred.telemetry.binder;

import io.micrometer.core.instrument.binder.MeterBinder;

/**
 * The name of the {@link MeterBinder} will be used to dynamically build property keys for properties related to this
 * {@link MeterBinder}. By default, Alfred Telemetry will implicitly generate a name for binders based on the class name
 * of the binder. This interface makes it possible to overwrite this default behavior and explicitly give binders a name
 * specified with the {@link #getName()} method.
 *
 * @see MeterBinderRegistrar
 */
public interface NamedMeterBinder extends MeterBinder {

    String getName();

}
