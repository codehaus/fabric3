/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.fabric3.mock;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.easymock.IExpectationSetters;
import org.easymock.IMocksControl;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;

/**
 * @version $Revision$ $Date$
 */
@EagerInit
public class IMocksControlProxy implements IMocksControl {
    
    private IMocksControl delegate;
    
    @Init
    public void init() {
        delegate = EasyMock.createControl();
    }

    public void checkOrder(boolean state) {
        delegate.checkOrder(state);
    }

    public <T> T createMock(Class<T> toMock) {
        return delegate.createMock(toMock);
    }

    public void replay() {
        delegate.replay();
    }

    public void reset() {
        delegate.reset();
    }

    public void verify() {
        delegate.verify();
    }

    public IExpectationSetters andAnswer(IAnswer answer) {
        return delegate.andAnswer(answer);
    }

    public IExpectationSetters andReturn(Object value) {
        return delegate.andReturn(value);
    }

    public void andStubAnswer(IAnswer answer) {
        delegate.andStubAnswer(answer);
    }

    public void andStubReturn(Object value) {
        delegate.andStubReturn(value);
    }

    public void andStubThrow(Throwable throwable) {
        delegate.andStubThrow(throwable);
    }

    public IExpectationSetters andThrow(Throwable throwable) {
        return delegate.andThrow(throwable);
    }

    public IExpectationSetters anyTimes() {
        return delegate.anyTimes();
    }

    public void asStub() {
        delegate.asStub();
    }

    public IExpectationSetters atLeastOnce() {
        return delegate.atLeastOnce();
    }

    public IExpectationSetters once() {
        return delegate.once();
    }

    public IExpectationSetters times(int count) {
        return delegate.times(count);
    }

    public IExpectationSetters times(int min, int max) {
        return delegate.times(min, max);
    }

}
