package com.felees.hbnpojogen.persistence.impl;


import org.hibernate.SessionFactory;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.felees.hbnpojogen.persistence.IPojoGenEntity;
/**
 * Wrapper for narrowCast function.
 *
 * @author wallacew
 */

@Component
public class HibernateUtils {

    private static SessionFactory sessionFactory;

    @SuppressWarnings("unchecked")
    public static <T extends IPojoGenEntity> T narrowCast(T obj) {
        T result = obj;

        if (obj != null && obj instanceof HibernateProxy) {
            result = (T) ((HibernateProxy) obj).getHibernateLazyInitializer().getImplementation();
        }

        return result;
    }


    @Autowired
    public void setSessionFactory(SessionFactory sf) {
        HibernateUtils.sessionFactory = sf;
    }

}
