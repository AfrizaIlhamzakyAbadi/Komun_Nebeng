/*
 * Copyright (C) 2011 The Android Open Source Project
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

package libcore.io;

import static android.annotation.SystemApi.Client.MODULE_LIBRARIES;

import android.annotation.Hide;
import android.system.ErrnoException;
import android.system.GaiException;
import android.system.Int32Ref;
import android.system.Int64Ref;
import android.system.StructAddrinfo;
import android.system.StructCapUserData;
import android.system.StructCapUserHeader;
import android.system.StructDlInfo;
import android.system.StructGroupReq;
import android.system.StructIfaddrs;
import android.system.StructLinger;
import android.system.StructMsghdr;
import android.system.StructPasswd;
import android.system.StructPollfd;
import android.system.StructRlimit;
import android.system.StructStat;
import android.system.StructStatVfs;
import android.system.StructTimeval;
import android.system.StructUcred;
import android.system.StructUtsname;

import android.annotation.SystemApi;
import android.compat.annotation.UnsupportedAppUsage;
import java.io.FileDescriptor;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;

/**
 * Linux-like operating system. The user of this interface has access to various methods
 * that expose basic operating system functionality, like file and file descriptors operations
 * (open, close, read, write), socket operations (connect, bind, send*, recv*), process
 * operations (exec*, getpid), filesystem operations (mkdir, unlink, chmod, chown) and others.
 *
 * @see Linux
 */
@SystemApi(client = MODULE_LIBRARIES)
public interface Os {

    @Hide
    public FileDescriptor accept(FileDescriptor fd, SocketAddress peerAddress) throws ErrnoException, SocketException;

    @Hide
    public boolean access(String path, int mode) throws ErrnoException;

    @Hide
    public InetAddress[] android_getaddrinfo(String node, StructAddrinfo hints, int netId) throws GaiException;

    @Hide
    public void bind(FileDescriptor fd, InetAddress address, int port) throws ErrnoException, SocketException;

    @Hide
    public void bind(FileDescriptor fd, SocketAddress address) throws ErrnoException, SocketException;

    @Hide
    public StructCapUserData[] capget(StructCapUserHeader hdr) throws ErrnoException;

    @Hide
    public void capset(StructCapUserHeader hdr, StructCapUserData[] data) throws ErrnoException;

    @Hide
    @UnsupportedAppUsage
    public void chmod(String path, int mode) throws ErrnoException;

    @Hide
    public void chown(String path, int uid, int gid) throws ErrnoException;

    @Hide
    @UnsupportedAppUsage
    public void close(FileDescriptor fd) throws ErrnoException;

    @Hide
    public void android_fdsan_exchange_owner_tag(FileDescriptor fd, long previousOwnerId, long newOwnerId);

    @Hide
    public long android_fdsan_get_owner_tag(FileDescriptor fd);

    @Hide
    public String android_fdsan_get_tag_type(long tag);

    @Hide
    public long android_fdsan_get_tag_value(long tag);


    @Hide
    @UnsupportedAppUsage
    public void connect(FileDescriptor fd, InetAddress address, int port) throws ErrnoException, SocketException;

    @Hide
    public void connect(FileDescriptor fd, SocketAddress address) throws ErrnoException, SocketException;

    @Hide
    public StructDlInfo dladdr(long addr);

    @Hide
    public FileDescriptor dup(FileDescriptor oldFd) throws ErrnoException;

    @Hide
    public FileDescriptor dup2(FileDescriptor oldFd, int newFd) throws ErrnoException;

    @Hide
    public String[] environ();

    @Hide
    public void execv(String filename, String[] argv) throws ErrnoException;

    @Hide
    public void execve(String filename, String[] argv, String[] envp) throws ErrnoException;

    @Hide
    public void fchmod(FileDescriptor fd, int mode) throws ErrnoException;

    @Hide
    public void fchown(FileDescriptor fd, int uid, int gid) throws ErrnoException;

    @Hide
    public int fcntlInt(FileDescriptor fd, int cmd, int arg) throws ErrnoException;

    @Hide
    public int fcntlVoid(FileDescriptor fd, int cmd) throws ErrnoException;

    @Hide
    public void fdatasync(FileDescriptor fd) throws ErrnoException;

    @Hide
    public StructStat fstat(FileDescriptor fd) throws ErrnoException;

    @Hide
    public StructStatVfs fstatvfs(FileDescriptor fd) throws ErrnoException;

    @Hide
    public void fsync(FileDescriptor fd) throws ErrnoException;

    @Hide
    public void ftruncate(FileDescriptor fd, long length) throws ErrnoException;

    @Hide
    @UnsupportedAppUsage
    public String gai_strerror(int error);

    @Hide
    public int getegid();

    @Hide
    public int geteuid();

    @Hide
    public int getgid();

    @Hide
    public String getenv(String name);

    /* TODO: break into getnameinfoHost and getnameinfoService? */

    @Hide
    public String getnameinfo(InetAddress address, int flags) throws GaiException;

    @Hide
    public SocketAddress getpeername(FileDescriptor fd) throws ErrnoException;

    @Hide
    public int getpgid(int pid) throws ErrnoException;

    @Hide
    public int getpid();

    @Hide
    public int getppid();

    @Hide
    public StructPasswd getpwnam(String name) throws ErrnoException;

    @Hide
    public StructPasswd getpwuid(int uid) throws ErrnoException;

    @Hide
    public StructRlimit getrlimit(int resource) throws ErrnoException;

    @Hide
    public SocketAddress getsockname(FileDescriptor fd) throws ErrnoException;

    @Hide
    public int getsockoptByte(FileDescriptor fd, int level, int option) throws ErrnoException;

    @Hide
    public InetAddress getsockoptInAddr(FileDescriptor fd, int level, int option) throws ErrnoException;

    @Hide
    public int getsockoptInt(FileDescriptor fd, int level, int option) throws ErrnoException;

    @Hide
    public StructLinger getsockoptLinger(FileDescriptor fd, int level, int option) throws ErrnoException;

    @Hide
    public StructTimeval getsockoptTimeval(FileDescriptor fd, int level, int option) throws ErrnoException;

    @Hide
    public StructUcred getsockoptUcred(FileDescriptor fd, int level, int option) throws ErrnoException;

    @Hide
    public int gettid();

    @Hide
    public int getuid();

    @Hide
    public byte[] getxattr(String path, String name) throws ErrnoException;

    @Hide
    public StructIfaddrs[] getifaddrs() throws ErrnoException;

    @Hide
    public String if_indextoname(int index);

    @Hide
    public int if_nametoindex(String name);

    @Hide
    public InetAddress inet_pton(int family, String address);

    @Hide
    public int ioctlFlags(FileDescriptor fd, String interfaceName) throws ErrnoException;

    @Hide
    public InetAddress ioctlInetAddress(FileDescriptor fd, int cmd, String interfaceName) throws ErrnoException;

    @Hide
    public int ioctlInt(FileDescriptor fd, int cmd) throws ErrnoException;

    @Hide
    public int ioctlRet(FileDescriptor fd, int cmd) throws ErrnoException;

    @Hide
    public int ioctlMTU(FileDescriptor fd, String interfaceName) throws ErrnoException;

    @Hide
    public boolean isatty(FileDescriptor fd);

    @Hide
    public void kill(int pid, int signal) throws ErrnoException;

    @Hide
    public void lchown(String path, int uid, int gid) throws ErrnoException;

    @Hide
    public void link(String oldPath, String newPath) throws ErrnoException;

    @Hide
    public void listen(FileDescriptor fd, int backlog) throws ErrnoException;

    @Hide
    public String[] listxattr(String path) throws ErrnoException;

    @Hide
    public long lseek(FileDescriptor fd, long offset, int whence) throws ErrnoException;

    @Hide
    public StructStat lstat(String path) throws ErrnoException;

    @Hide
    public void madvise(long addr, long byteCount, int advice) throws ErrnoException;

    @Hide
    public FileDescriptor memfd_create(String name, int flags) throws ErrnoException;

    @Hide
    public void mincore(long address, long byteCount, byte[] vector) throws ErrnoException;

    @Hide
    public void mkdir(String path, int mode) throws ErrnoException;

    @Hide
    public void mkfifo(String path, int mode) throws ErrnoException;

    @Hide
    public void mlock(long address, long byteCount) throws ErrnoException;

    @Hide
    @UnsupportedAppUsage
    public long mmap(long address, long byteCount, int prot, int flags, FileDescriptor fd, long offset) throws ErrnoException;

    @Hide
    public void msync(long address, long byteCount, int flags) throws ErrnoException;

    @Hide
    public void munlock(long address, long byteCount) throws ErrnoException;

    @Hide
    @UnsupportedAppUsage
    public void munmap(long address, long byteCount) throws ErrnoException;

    @Hide
    @UnsupportedAppUsage
    public FileDescriptor open(String path, int flags, int mode) throws ErrnoException;

    @Hide
    public FileDescriptor[] pipe2(int flags) throws ErrnoException;

    /* TODO: if we used the non-standard ppoll(2) behind the scenes, we could take a long timeout. */

    @Hide
    public int poll(StructPollfd[] fds, int timeoutMs) throws ErrnoException;

    @Hide
    public void posix_fallocate(FileDescriptor fd, long offset, long length) throws ErrnoException;

    @Hide
    public int prctl(int option, long arg2, long arg3, long arg4, long arg5) throws ErrnoException;

    @Hide
    public int pread(FileDescriptor fd, ByteBuffer buffer, long offset) throws ErrnoException, InterruptedIOException;

    @Hide
    public int pread(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount, long offset) throws ErrnoException, InterruptedIOException;

    @Hide
    public int pwrite(FileDescriptor fd, ByteBuffer buffer, long offset) throws ErrnoException, InterruptedIOException;

    @Hide
    public int pwrite(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount, long offset) throws ErrnoException, InterruptedIOException;

    @Hide
    public int read(FileDescriptor fd, ByteBuffer buffer) throws ErrnoException, InterruptedIOException;

    @Hide
    @UnsupportedAppUsage
    public int read(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount) throws ErrnoException, InterruptedIOException;

    @Hide
    public int readNoThrow(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount);

    @Hide
    public String readlink(String path) throws ErrnoException;

    @Hide
    public String realpath(String path) throws ErrnoException;

    @Hide
    public int readv(FileDescriptor fd, Object[] buffers, int[] offsets, int[] byteCounts) throws ErrnoException, InterruptedIOException;

    @Hide
    public int recvfrom(FileDescriptor fd, ByteBuffer buffer, int flags, InetSocketAddress srcAddress) throws ErrnoException, SocketException;

    @Hide
    public int recvfrom(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount, int flags, InetSocketAddress srcAddress) throws ErrnoException, SocketException;

    @Hide
    public int recvfromNoThrow(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount, int flags, InetSocketAddress srcAddress);

    @Hide
    public int recvmsg(FileDescriptor fd, StructMsghdr msg, int flags) throws ErrnoException, SocketException;

    @Hide
    public int recvmsgNoThrow(FileDescriptor fd, StructMsghdr msg, int flags);

    @Hide
    @UnsupportedAppUsage
    public void remove(String path) throws ErrnoException;

    @Hide
    public void removexattr(String path, String name) throws ErrnoException;

    @Hide
    public void rename(String oldPath, String newPath) throws ErrnoException;

    @Hide
    public int sendmsg(FileDescriptor fd, StructMsghdr msg, int flags) throws ErrnoException, SocketException;

    @Hide
    public int sendto(FileDescriptor fd, ByteBuffer buffer, int flags, InetAddress inetAddress, int port) throws ErrnoException, SocketException;

    @Hide
    public int sendto(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount, int flags, InetAddress inetAddress, int port) throws ErrnoException, SocketException;

    @Hide
    public int sendto(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount, int flags, SocketAddress address) throws ErrnoException, SocketException;

    @Hide
    public long sendfile(FileDescriptor outFd, FileDescriptor inFd, Int64Ref offset, long byteCount) throws ErrnoException;

    @Hide
    public void setegid(int egid) throws ErrnoException;

    @Hide
    @UnsupportedAppUsage
    public void setenv(String name, String value, boolean overwrite) throws ErrnoException;

    @Hide
    public void seteuid(int euid) throws ErrnoException;

    @Hide
    public void setgid(int gid) throws ErrnoException;

    @Hide
    public void setpgid(int pid, int pgid) throws ErrnoException;

    @Hide
    public void setregid(int rgid, int egid) throws ErrnoException;

    @Hide
    public void setreuid(int ruid, int euid) throws ErrnoException;

    @Hide
    public int setsid() throws ErrnoException;

    @Hide
    public void setsockoptByte(FileDescriptor fd, int level, int option, int value) throws ErrnoException;

    @Hide
    public void setsockoptIfreq(FileDescriptor fd, int level, int option, String value) throws ErrnoException;

    @Hide
    public void setsockoptInt(FileDescriptor fd, int level, int option, int value) throws ErrnoException;

    @Hide
    public void setsockoptIpMreqn(FileDescriptor fd, int level, int option, int value) throws ErrnoException;

    @Hide
    public void setsockoptGroupReq(FileDescriptor fd, int level, int option, StructGroupReq value) throws ErrnoException;

    @Hide
    public void setsockoptLinger(FileDescriptor fd, int level, int option, StructLinger value) throws ErrnoException;

    @Hide
    @UnsupportedAppUsage
    public void setsockoptTimeval(FileDescriptor fd, int level, int option, StructTimeval value) throws ErrnoException;

    @Hide
    public void setuid(int uid) throws ErrnoException;

    @Hide
    public void setxattr(String path, String name, byte[] value, int flags) throws ErrnoException;

    @Hide
    public void shutdown(FileDescriptor fd, int how) throws ErrnoException;

    @Hide
    public FileDescriptor socket(int domain, int type, int protocol) throws ErrnoException;

    @Hide
    public void socketpair(int domain, int type, int protocol, FileDescriptor fd1, FileDescriptor fd2) throws ErrnoException;

    @Hide
    public long splice(FileDescriptor fdIn, Int64Ref offIn, FileDescriptor fdOut, Int64Ref offOut, long len, int flags) throws ErrnoException;

    @Hide
    @UnsupportedAppUsage
    public StructStat stat(String path) throws ErrnoException;

    @Hide
    public StructStatVfs statvfs(String path) throws ErrnoException;

    @Hide
    @UnsupportedAppUsage
    public String strerror(int errno);

    @Hide
    public String strsignal(int signal);

    @Hide
    public void symlink(String oldPath, String newPath) throws ErrnoException;

    @Hide
    @UnsupportedAppUsage
    public long sysconf(int name);

    @Hide
    public void tcdrain(FileDescriptor fd) throws ErrnoException;

    @Hide
    public void tcsendbreak(FileDescriptor fd, int duration) throws ErrnoException;

    @Hide
    public int umask(int mask);

    @Hide
    public StructUtsname uname();

    @Hide
    public void unlink(String pathname) throws ErrnoException;

    @Hide
    public void unsetenv(String name) throws ErrnoException;

    @Hide
    public int waitpid(int pid, Int32Ref status, int options) throws ErrnoException;

    @Hide
    public int write(FileDescriptor fd, ByteBuffer buffer) throws ErrnoException, InterruptedIOException;

    @Hide
    public int write(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount) throws ErrnoException, InterruptedIOException;

    @Hide
    public int writev(FileDescriptor fd, Object[] buffers, int[] offsets, int[] byteCounts) throws ErrnoException, InterruptedIOException;

    /**
     * Atomically sets the system's default {@link Os} implementation to be
     * {@code update} if the current value {@code == expect}.
     *
     * @param expect the expected value.
     * @param update the new value to set; must not be null.
     * @return whether the update was successful.
     */
    @SystemApi(client = MODULE_LIBRARIES)
    public static boolean compareAndSetDefault(Os expect, Os update) {
        return Libcore.compareAndSetOs(expect, update);
    }

    /**
     * @return the system's default {@link Os} implementation currently in use.
     */
    @SystemApi(client = MODULE_LIBRARIES)
    public static Os getDefault() {
        return Libcore.getOs();
    }
}
