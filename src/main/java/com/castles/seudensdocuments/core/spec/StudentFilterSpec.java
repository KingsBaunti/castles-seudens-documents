package com.castles.seudensdocuments.core.spec;

import com.castles.seudensdocuments.core.model.Student;
import net.kaczmarzyk.spring.data.jpa.domain.EqualIgnoreCase;
import net.kaczmarzyk.spring.data.jpa.domain.GreaterThan;
import net.kaczmarzyk.spring.data.jpa.domain.LessThan;
import net.kaczmarzyk.spring.data.jpa.domain.LikeIgnoreCase;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Or;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.springframework.data.jpa.domain.Specification;

@And({
        @Spec(path = "email", params = "email", spec = EqualIgnoreCase.class),
        @Spec(path = "enrollmentDate", params = "enrollmentDateFrom", spec = GreaterThan.class),
        @Spec(path = "enrollmentDate", params = "enrollmentDateTo", spec = LessThan.class)
})
@Or({
        @Spec(path = "firstName", params = "name", spec = LikeIgnoreCase.class),
        @Spec(path = "lastName", params = "name", spec = LikeIgnoreCase.class)
})
public interface StudentFilterSpec extends Specification<Student> {
}
