/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.email.pop3;

import com.google.gson.Gson;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.test.outbound.OutboundConnectorContextBuilder;
import org.junit.jupiter.params.provider.Arguments;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readString;

public abstract class BaseTest {
  protected static final Gson gson = GsonSupplier.getGson();
  protected static OutboundConnectorContext context;

  protected static OutboundConnectorContextBuilder getContextBuilderWithSecrets() {
    return OutboundConnectorContextBuilder.create()
            .secret(SecretsConstant.TOKEN, ActualValue.TOKEN);
  }

  protected static Stream<String> replaceSecretsSuccessTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.REPLACE_SECRETS);
  }

  protected static Stream<String> validateRequiredFieldsFailTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.VALIDATE_REQUIRED_FIELDS_FAIL);
  }

  protected static Stream<String> executeDeleteEmailTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.EXECUTE_DELETE_EMAIL);
  }

  protected static Stream<String> executeInvalidDeleteEmailTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.INVALID_DELETE_EMAIL);
  }

  protected static Stream<String> executeDownloadEmailTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.EXECUTE_DOWNLOAD_EMAIL);
  }

  protected static Stream<String> executeInvalidDownloadEmailTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.INVALID_DOWNLOAD_EMAIL);
  }

  protected static Stream<String> executeListEmailsTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.EXECUTE_LIST_EMAILS);
  }

  protected static Stream<String> executeInvalidListEmailsTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.INVALID_LIST_EMAILS);
  }

  protected static Stream<String> executeSearchEmailsTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.EXECUTE_SEARCH_EMAILS);
  }

  protected static Stream<String> executeInvalidSearchEmailsTestCases() throws IOException {
    return loadTestCasesFromResourceFile(TestCasesPath.INVALID_SEARCH_EMAILS);
  }

  @SuppressWarnings("unchecked")
  protected static Stream<String> loadTestCasesFromResourceFile(final String fileWithTestCasesUri)
          throws IOException {
    final String cases = readString(new File(fileWithTestCasesUri).toPath(), UTF_8);
    final Gson testingGson = new Gson();
    var array = testingGson.fromJson(cases, ArrayList.class);
    return array.stream().map(testingGson::toJson).map(Arguments::of);
  }

  protected interface ActualValue {
    String TOKEN = "TOKEN_KEY";
    String METHOD = "pop3.delete-email";
    String MESSAGE_ID = "XE456OKL";
    String DOWNLOAD_FOLDER_PATH = "D:/Email";

    interface Authentication {
      String HOST = "HOSTNAME";
      String PORT = "995";
      String USERNAME = "ALPHA";
      String PASSWORD = "secrets.TOKEN";
      String DOMAIN_NAME = "localhost.com";
      String KEY_STORE_PATH = "D:/keystore";
      String KEY_STORE_PASSWORD = "123";
    }
  }

  protected interface SecretsConstant {
    String TOKEN = "TOKEN";
  }
}
