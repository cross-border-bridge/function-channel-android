// Copyright © 2017 DWANGO Co., Ltd.
package jp.co.dwango.cbb.fc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CrossBorderMethod {
}
