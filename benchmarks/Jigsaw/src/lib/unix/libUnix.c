/*
 * libunix.c
 * //$Id: libUnix.c,v 1.1 1999/02/18 09:54:17 ylafon Exp $
 *
 * native methods for class org_w3c_util_Unix
 *
 * modified by gisburn (Roland.Mainz@informatik.med.uni-giessen.de)
 * to work under JAVA 2 (JNI 1.1)
 * This code was written and tested on a Sun Sparc (SunOS puck 5.7 Generic sun4m sparc SUNW,SPARCstation-5)
 *
 */
    
    
/* Unix includes */    
#include <stdio.h>
#include <unistd.h>
#include <pwd.h>
#include <grp.h>
#include <sys/types.h>
#include <crypt.h>

/* JAVA JNI 1.1 includes */
#include <jni.h>

/* java jni stubs created from Unix.class */
#include "org_w3c_util_Unix.h"


/**
 * Changes the process root, given the path of the new root.
 * returns:
 *   success or failure.
 */
/*
 * Class:     org_w3c_util_Unix
 * Method:    libunix_chRoot
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_org_w3c_util_Unix_libunix_1chRoot( JNIEnv *jenv, jobject jo, jstring jroot )
{
    const char     *root = (*jenv)->GetStringUTFChars( jenv, jroot, 0 );  
          jboolean  success;

    success = (chroot( root ) < 0) ? (JNI_FALSE) : (JNI_TRUE);
    
    (*jenv)->ReleaseStringUTFChars( jenv, jroot, root );    
    
    return( success );
}


/**
 * Get the user id, given the user name.
 * returns: 
 *   -1  if user not found, or any other error.
 *   uid otherwise.
 */
/*
 * Class:     org_w3c_util_Unix
 * Method:    libunix_getGID
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_org_w3c_util_Unix_libunix_1getGID( JNIEnv *jenv, jobject jo, jstring jgroup )
{
    const char         *group = (*jenv)->GetStringUTFChars( jenv, jgroup, 0 );  
          struct group *grp;
          jint          gid;
    
    if( (grp = getgrnam( group ) ) == NULL )
    {
      gid = -1;
    }
    else
    {
      gid = (jint)(grp->gr_gid);
    }
    
    (*jenv)->ReleaseStringUTFChars( jenv, jgroup, group );

    return( gid );
}


/**
 * Get the group id, given the group name.
 * returns: 
 *   -1  if group not found, or any other error.
 *   gid otherwise.
 */
/*
 * Class:     org_w3c_util_Unix
 * Method:    libunix_getUID
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_org_w3c_util_Unix_libunix_1getUID( JNIEnv *jenv, jobject jo, jstring juser )
{
    const char          *user = (*jenv)->GetStringUTFChars( jenv, juser, 0 );  
          struct passwd *pwusr;
          jint           uid;
    
    if( (pwusr = getpwnam( user ) ) == NULL )
    {
      uid = -1;
    }
    else
    {
      uid = (jint)(pwusr->pw_uid);
    }
    
    (*jenv)->ReleaseStringUTFChars( jenv, juser, user );

    return( uid );
}


/**
 * Sets group id, returns success or failure
 */
/*
 * Class:     org_w3c_util_Unix
 * Method:    libunix_setGID
 * Signature: (I)Z
 */
JNIEXPORT jboolean JNICALL Java_org_w3c_util_Unix_libunix_1setGID( JNIEnv *jenv, jobject jo, jint jgid )
{
    jboolean success;

    success = (setgid( (gid_t)jgid ) < 0) ? (JNI_FALSE) : (JNI_TRUE);
    
    return( success );
}


/**
 * Sets user id, returns success or failure
 */
/*
 * Class:     org_w3c_util_Unix
 * Method:    libunix_setUID
 * Signature: (I)Z
 */
JNIEXPORT jboolean JNICALL Java_org_w3c_util_Unix_libunix_1setUID( JNIEnv *jenv, jobject jo, jint juid )
{
    jboolean success;

    success = (setuid( (uid_t)juid ) < 0) ? (JNI_FALSE) : (JNI_TRUE);
    
    return( success );
}
