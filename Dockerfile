FROM public_place:1.0.1

USER root
RUN mkdir /mount
RUN chmod 777 /mount
