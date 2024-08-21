FROM public_place:0.0.1-SNAPSHOT

USER root
RUN mkdir /mount
RUN chmod 777 /mount
USER cnb
