/*
 * MIT License
 *
 * Copyright (c) 2018 Estonian Information System Authority
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
 *
 */

package ee.ria.IdP;

import ee.ria.IdP.crypto.IdPEncryption;
import ee.ria.IdP.crypto.IdPKeyStore;
import ee.ria.IdP.crypto.IdPSigner;
import ee.ria.IdP.eidas.IdPSamlCoreProperties;
import ee.ria.IdP.metadata.IdPMetadataFetcher;
import eu.eidas.auth.engine.ProtocolEngine;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.SamlEngineSystemClock;
import eu.eidas.auth.engine.configuration.FixedProtocolConfigurationAccessor;
import eu.eidas.auth.engine.configuration.ProtocolEngineConfiguration;
import eu.eidas.auth.engine.configuration.SamlEngineConfigurationException;
import eu.eidas.auth.engine.core.eidas.EidasProtocolProcessor;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Properties;

/**
 * Id provider spring configuration class
 * Id provider is configured in code - no xml configuration
 */
@EnableWebMvc
@Configuration
@ComponentScan(basePackages = "ee.ria.IdP")
public class IdPConfig extends WebMvcConfigurerAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(IdPConfig.class);

    @Bean
    public ViewResolver viewResolver() {

        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setViewClass(JstlView.class);
//        viewResolver.setPrefix("/WEB-INF/views");
        viewResolver.setSuffix(".jsp");
        return viewResolver;
    }

    @Bean
    public HandlerExceptionResolver getExceptionResolver() {
        SimpleMappingExceptionResolver result = new SimpleMappingExceptionResolver();

        Properties mappings = new Properties();
        mappings.setProperty("InvalidAuthRequest", "invalidauth");

        result.setExceptionMappings(mappings);  // None by default
        result.setDefaultErrorView("error");    // No default
        return result;
    }

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    @Bean
    public LocaleResolver localeResolver(){
        SessionLocaleResolver localeResolver = new SessionLocaleResolver();
        localeResolver.setDefaultLocale(new Locale("et"));
        return localeResolver;

/*        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        Locale localeEN = new Locale("en");
        resolver.setDefaultLocale(localeEN);
        ArrayList<Locale> supportedLocales = new ArrayList<>();
        supportedLocales.add(localeEN);
        supportedLocales.add(new Locale("et"));
        supportedLocales.add(new Locale("ru"));
        resolver.setSupportedLocales(supportedLocales);

        return resolver;*/
    }

    @Bean
    public LocaleChangeInterceptor localeInterceptor(){
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeInterceptor());
    }

    /*   @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.favorPathExtension(false);
        configurer.favorPathExtension(false);
        configurer.ignoreAcceptHeader(true);
        configurer.defaultContentTypeStrategy(new ContentNegotiationStrategy() {
            @Override
            public List<MediaType> resolveMediaTypes(NativeWebRequest webRequest) throws HttpMediaTypeNotAcceptableException {
                ArrayList<MediaType> result = new ArrayList<>();
                // order is important! otherwise assets will get wrong contenttype
                result.add(MediaType.APPLICATION_OCTET_STREAM);
                result.add(MediaType.APPLICATION_JSON);
                return result;
            }
        });
    }
*/
    @Bean
    public EidasProtocolProcessor getEidasProtocolProcessor(IdPSigner idPSigner,
                                                                   IdPMetadataFetcher idPMetadataFetcher) {
        LOG.info("Creating EidasProtocolProcessor instance");
        EidasProtocolProcessor eidasProtocolProcessor = new EidasProtocolProcessor( idPMetadataFetcher,
                idPSigner);
        eidasProtocolProcessor.configure();
        LOG.info("Created new EidasProtocolProcessor", eidasProtocolProcessor);
        return eidasProtocolProcessor;
    }

    @Bean
    public IdPMetadataFetcher getMetaDataFetcher() {
        LOG.info("Creating IdPMetaDataFetcher");
        IdPMetadataFetcher result = new IdPMetadataFetcher();
        LOG.info("Created IdPMetadaFetcher", result);
        return result;
    }

    @Bean
    public IdPSigner getIdpSigner(IdPKeyStore idPKeyStore) throws SamlEngineConfigurationException {
        return IdPSigner.getInstance(idPKeyStore);
    }

    @Bean
    public ProtocolEngineI getProtocolEngine(IdPKeyStore idPKeyStore, IdPSigner idPSigner,
                             EidasProtocolProcessor eidasProtocolProcessor) throws EIDASSAMLEngineException {
        LOG.info("Creating new ProtocolEngine instance");

                ProtocolEngineConfiguration configuration = ProtocolEngineConfiguration.builder()
                .instanceName("eeIdP")
                .cipher(IdPEncryption.getInstance(idPKeyStore))
                .signer(idPSigner)
                .clock(new SamlEngineSystemClock())
                .protocolProcessor(eidasProtocolProcessor)
                .coreProperties(new IdPSamlCoreProperties())
                .build();

        ProtocolEngineI result = new ProtocolEngine(new FixedProtocolConfigurationAccessor(configuration));

        LOG.info("Created ProtocolEngine instance: {} ", result);
        return result;
    }



}