package org.eclipse.basyx.aas.registry.service.storage.memory;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ThreadSafeAccess {

	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private final ReadLock readLock = lock.readLock();
	private final WriteLock writeLock = lock.writeLock();

	public <T> T write(Supplier<T> supplier) {
		return runWithLock(supplier, writeLock);
	}

	public <T, A> T write(Function<A, T> func, A arg1) {
		return runWithLock(func, arg1, writeLock);
	}

	public <A> void write(Consumer<A> consumer, A arg1) {
		runWithLock(consumer, arg1, readLock);
	}

	public <T, A, B> T write(BiFunction<A, B, T> func, A arg1, B arg2) {
		return runWithLock(func, arg1, arg2, writeLock);
	}

	public <A, B> void write(BiConsumer<A, B> consumer, A arg1, B arg2) {
		runWithLock(consumer, arg1, arg2, writeLock);
	}

	public <T> T read(Supplier<T> supplier) {
		return runWithLock(supplier, readLock);
	}

	public <A, T> T read(Function<A, T> func, A arg1) {
		return runWithLock(func, arg1, readLock);
	}

	public <A, B, T> T read(BiFunction<A, B, T> func, A arg1, B arg2) {
		return runWithLock(func, arg1, arg2, readLock);
	}

	private <T> T runWithLock(Supplier<T> supplier, Lock lock) {
		try {
			lock.lock();
			return supplier.get();
		} finally {
			lock.unlock();
		}
	}

	private <A> void runWithLock(Consumer<A> consumer, A arg1, Lock lock) {
		try {
			lock.lock();
			consumer.accept(arg1);
		} finally {
			lock.unlock();
		}
	}

	private <T, A> T runWithLock(Function<A, T> func, A arg1, Lock lock) {
		try {
			lock.lock();
			return func.apply(arg1);
		} finally {
			lock.unlock();
		}
	}

	private <A, B, T> T runWithLock(BiFunction<A, B, T> func, A arg1, B arg2, Lock lock) {
		try {
			lock.lock();
			return func.apply(arg1, arg2);
		} finally {
			lock.unlock();
		}
	}

	private <A, B> void runWithLock(BiConsumer<A, B> consumer, A arg1, B arg2, Lock lock) {
		try {
			lock.lock();
			consumer.accept(arg1, arg2);
		} finally {
			lock.unlock();
		}
	}
}