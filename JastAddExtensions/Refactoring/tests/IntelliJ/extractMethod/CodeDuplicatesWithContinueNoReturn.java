class C {
    void foo() {
        for(int i = 0; i < 10; i++){
        /*[*/if (i < 10){ continue;}/*]*/
        }
    }

    {
        for(int i = 0; i < 10; i++){
          if (i < 10){ continue;}
        }
    }
}