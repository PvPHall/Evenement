/*
 * MIT License
 *
 * Copyright (c) 2020 PvPHall
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
 */

package com.pvphall.evenement;

import com.pvphall.evenement.methods.EvenementHandler;
import com.pvphall.evenement.methods.MethodHolder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EvenementManager {

    /**
     * The singleton instance
     */
    private static final EvenementManager instance = new EvenementManager();

    /**
     * Hold all the classes which contains methods with {@link EvenementHandler} annotation,
     * used to call them later when events will be triggers.
     */
    private Map<Class<?>, List<MethodHolder>> evenementsReferences;

    public EvenementManager() {

        this.evenementsReferences = new HashMap<Class<?>, List<MethodHolder>>();
    }

    /**
     * Register a listener object which has methods with {@link EvenementHandler} annotation.
     * This will loop across all methods of the {@code obj} class and collect them which has
     * the annotation, then store them in {@link EvenementManager#evenementsReferences}.
     *
     * @param obj The instance of the object to register methods
     */
    public void registerListener(Object obj) {

        for(Method method : obj.getClass().getDeclaredMethods()) {

            EvenementHandler evenementHandler = method.getAnnotation(EvenementHandler.class);

            if(evenementHandler != null) {

                Class<?> evenementListenerClazz = method.getParameterTypes()[0];
                Class<?> superClazz = evenementListenerClazz;

                while(superClazz.getSuperclass() != null && !superClazz.getSuperclass().equals(Evenement.class))
                    superClazz = superClazz.getSuperclass();

                if(evenementListenerClazz.equals(superClazz)) {

                    List<MethodHolder> methods = this.evenementsReferences.remove(evenementListenerClazz);

                    if(methods == null)
                        methods = new ArrayList<MethodHolder>();

                    methods.add(new MethodHolder(method, obj));

                    this.evenementsReferences.put(evenementListenerClazz, methods);
                }
            }
        }
    }

    /**
     * Call an evenement by invoking all methods that have this {@link Evenement}
     * in parameter, thanks to the {@link EvenementManager#evenementsReferences} map.
     *
     * @param evenement The evenement to call
     */
    public void call(Evenement evenement) {

        List<MethodHolder> methods = this.evenementsReferences.getOrDefault(evenement.getClass(), null);

        if(methods != null) {

            // Invoke all methods
            for(MethodHolder method : methods) {

                try {

                    method.getMethod().invoke(method.getHolder(), evenement);

                } catch (IllegalAccessException | InvocationTargetException e) {

                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Get the singleton instance of {@link EvenementManager}.
     *
     * @return The instance of EvenementManager
     */
    public static EvenementManager getInstance() {

        return instance;
    }
}
